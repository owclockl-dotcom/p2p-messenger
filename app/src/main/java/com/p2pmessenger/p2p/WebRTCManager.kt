package com.p2pmessenger.p2p

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.*
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    private val context: Context
) {
    
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    
    private val factory: PeerConnectionFactory by lazy {
        initializePeerConnectionFactory()
    }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _receivedMessages = MutableStateFlow<String>("")
    val receivedMessages: StateFlow<String> = _receivedMessages
    
    private fun initializePeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        
        PeerConnectionFactory.initialize(options)
        
        return PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = false
                disableNetworkMonitor = false
            })
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(context))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    context,
                    true,
                    true
                )
            )
            .createPeerConnectionFactory()
    }
    
    suspend fun createOffer(): String {
        val iceServers = listOf(
            IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            iceTransportPolicy = PeerConnection.IceTransportPolicy.ALL
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            candidateNetworkPolicy = PeerConnection.NetworkPolicy.ALL
            keyType = PeerConnection.KeyType.ECDSA
        }
        
        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                // Handle ICE candidates
            }
            
            override fun onSignalingChange(state: PeerConnection.SignalingState) {
                when (state) {
                    PeerConnection.SignalingState.STABLE -> _connectionState.value = ConnectionState.Connected
                    PeerConnection.SignalingState.CLOSED -> _connectionState.value = ConnectionState.Disconnected
                    else -> {}
                }
            }
            
            override fun onDataChannel(channel: DataChannel) {
                setupDataChannel(channel)
            }
        })
        
        // Create data channel for messaging
        val dataChannelInit = DataChannel.Init().apply {
            ordered = true
            protocol = "messaging"
        }
        dataChannel = peerConnection?.createDataChannel("messages", dataChannelInit)
        setupDataChannel(dataChannel!!)
        
        val offer = peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints()) ?: return ""
        
        return offer.description
    }
    
    suspend fun createAnswer(offer: String): String {
        val iceServers = listOf(
            IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            iceTransportPolicy = PeerConnection.IceTransportPolicy.ALL
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            candidateNetworkPolicy = PeerConnection.NetworkPolicy.ALL
            keyType = PeerConnection.KeyType.ECDSA
        }
        
        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                // Handle ICE candidates
            }
            
            override fun onSignalingChange(state: PeerConnection.SignalingState) {
                when (state) {
                    PeerConnection.SignalingState.STABLE -> _connectionState.value = ConnectionState.Connected
                    PeerConnection.SignalingState.CLOSED -> _connectionState.value = ConnectionState.Disconnected
                    else -> {}
                }
            }
            
            override fun onDataChannel(channel: DataChannel) {
                setupDataChannel(channel)
            }
        })
        
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.OFFER, offer))
        
        val answer = peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints()) ?: return ""
        
        return answer.description
    }
    
    suspend fun setRemoteAnswer(answer: String) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.ANSWER, answer))
    }
    
    private fun setupDataChannel(channel: DataChannel) {
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {}
            
            override fun onStateChange() {
                when (channel.state()) {
                    DataChannel.State.OPEN -> _connectionState.value = ConnectionState.Connected
                    DataChannel.State.CLOSED -> _connectionState.value = ConnectionState.Disconnected
                    else -> {}
                }
            }
            
            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = buffer.data
                val bytes = ByteArray(data.remaining())
                data.get(bytes)
                _receivedMessages.value = String(bytes)
            }
        })
    }
    
    fun sendMessage(message: String): Boolean {
        val buffer = ByteBuffer.wrap(message.toByteArray())
        return dataChannel?.send(DataChannel.Buffer(buffer, false)) ?: false
    }
    
    fun disconnect() {
        dataChannel?.close()
        peerConnection?.close()
        peerConnection = null
        dataChannel = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Failed : ConnectionState()
    }
    
    private abstract class PeerConnectionObserver : PeerConnection.Observer {
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidate(p0: IceCandidate?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onAddStream(p0: MediaStream?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onDataChannel(p0: DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
    }
    
    private abstract class SdpObserver : org.webrtc.SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
