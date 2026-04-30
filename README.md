# P2P Messenger

A decentralized, serverless messaging application for Android with end-to-end encryption.

## Features

- **P2P Communication**: Direct device-to-device communication using WebRTC
- **End-to-End Encryption**: Signal Protocol for secure messaging
- **No Servers**: Your device acts as the server and storage
- **Telegram-Inspired Design**: Clean, modern UI similar to Telegram
- **QR Code Peer Discovery**: Easy connection via QR code scanning
- **Local Storage**: All messages stored locally on your device
- **Background Service**: Receive messages even when app is in background

## Architecture

### P2P Communication
- Uses WebRTC for direct peer-to-peer connections
- STUN servers for NAT traversal (public Google STUN)
- Data channels for message transmission

### Encryption
- Signal Protocol (Double Ratchet Algorithm)
- X3DH Key Exchange Protocol
- Perfect Forward Secrecy
- Public key authentication

### Storage
- Room Database for local message storage
- DataStore for encryption key storage
- No cloud storage - everything stays on device

### UI/UX
- Jetpack Compose for modern Android UI
- Material Design 3
- Telegram-inspired color scheme and layout
- Smooth animations and transitions

## Project Structure

```
app/
├── src/main/java/com/p2pmessenger/
│   ├── crypto/              # Encryption (Signal Protocol)
│   ├── data/
│   │   ├── database/        # Room Database
│   │   ├── model/           # Data models
│   │   └── repository/      # Repository pattern
│   ├── p2p/                 # WebRTC P2P manager
│   ├── service/             # Background service
│   ├── ui/
│   │   ├── screens/         # Compose screens
│   │   ├── theme/           # App theme
│   │   ├── viewmodel/       # ViewModels
│   │   └── navigation/      # Navigation
│   ├── MainActivity.kt
│   └── P2PMessengerApp.kt
```

## Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Room**: Local database
- **WebRTC**: P2P communication
- **Signal Protocol**: End-to-end encryption
- **ZXing**: QR code generation/scanning
- **Coroutines**: Asynchronous programming
- **DataStore**: Key-value storage

## How It Works

1. **Initialization**: App generates identity key pair using Signal Protocol
2. **Peer Discovery**: Users share QR codes containing public keys
3. **Connection**: WebRTC establishes direct P2P connection
4. **Messaging**: Messages encrypted with Signal Protocol, sent via WebRTC data channel
5. **Storage**: All messages stored locally in Room database

## Building

```bash
# Clone the repository
git clone <repository-url>
cd p2p-messenger

# Build the project
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

## Security

- All messages are end-to-end encrypted
- No server stores your messages
- Public keys are exchanged via QR codes
- Perfect Forward Secrecy ensures past messages remain secure even if keys are compromised
- No metadata collection

## Limitations

- Both devices must be online simultaneously for direct communication
- No offline message delivery (unless using relay peers)
- Battery usage may be higher due to background P2P service
- NAT traversal may require STUN/TURN servers

## Future Enhancements

- DHT-based peer discovery for decentralized peer finding
- Relay network for offline message delivery
- Voice and video calls via WebRTC
- File transfer support
- Group chats (multi-party E2E encryption)
- Tor integration for anonymity

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Disclaimer

This is a demonstration project. For production use, additional security audits and testing are recommended.
