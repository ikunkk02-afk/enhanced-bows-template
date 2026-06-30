# Enhanced Bows

A Fabric 1.21.1 mod that adds a server-authoritative spectral-arrow scanning mode.

## Spectral Arrow Scan

- Fire a spectral arrow clearly upward to start a five-second scan.
- Visible living entities inside the configured radius receive Glowing.
- Solid blocks obstruct scans, so walls and buildings divide scan spaces.
- Activated scanning arrows can bounce from blocks up to three times by default.
- Scanning and detected HUD animations have independent draggable positions and scales.

Open the HUD editor with Right Alt or with the button in the Mod Menu configuration screen.

## Build

```powershell
.\gradlew.bat clean test build
```

The built mod JAR is written to `build/libs/`.

## License

MIT
