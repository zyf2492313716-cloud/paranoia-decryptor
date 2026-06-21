# Paranoia File & Text Encryption Decryptor

Command-line wrapper for decrypting `.enc` files created by **Paranoia File & Text Encryption** (P.T.E.), a closed-source Java tool.

## Requirements

- Java 8+ (tested with OpenJDK 11)
- The `pfte.jar` from Paranoia File & Text Encryption tool

## Quick Start

```bash
# Place pfte.jar in the lib/ directory
cp /path/to/pfte.jar lib/pfte.jar

# Compile the wrapper
javac -cp lib/pfte.jar src/DecryptPTE.java -d out/

# Run decryption
java -Xmx24g -cp lib/pfte.jar:out DecryptPTE <input.enc> <output_dir> <password>
```

### Example

```bash
java -Xmx24g -cp lib/pfte.jar:out DecryptPTE \
  ~/Desktop/encrypted_file.enc \
  ~/Desktop/decrypted_output \
  YOUR_PASSWORD
```

> **Note:** `-Xmx24g` allocates 24GB heap memory. Adjust based on your system and file size. Argon2id key derivation is memory-intensive (20GB+).

## How It Works

The wrapper instantiates P.T.E.'s internal `Encryptor` class and calls `unzipAndDecryptFile()` directly, bypassing the GUI.

## File Format (Version 4)

```
Offset  Size   Description
------  ----   -----------
0-4     5      Magic bytes: "SSEFE"
5       1      Format version (0x04)
6       1      Algorithm code (0x00 = AES-256)
7       1      Custom params byte (Argon2id parameters)
8-39    32     Salt for Argon2id key derivation
40-N    var    Encrypted ZIP data (first 32 bytes = check code)
N-32    32     MAC (Message Authentication Code)
```

### Algorithm Codes

| Code | Algorithm      |
|------|---------------|
| 0    | AES-256       |
| 1    | RC6-256       |
| 2    | Serpent-256   |
| 3    | Blowfish-256  |
| 4    | Twofish-256   |

### Key Derivation (V4)

1. **Password -> L0 Hash**: `HKDF(Skein-1024-1024, password, salt="memorySalt", info="memoryInfo")` -> 256-byte `l0PWHashV3`
2. **Argon2id**: `Argon2id(password=l0PWHashV3, salt=fileSalt, t=20, m=20480, p=4, hashLen=32)` -> `argonOutput`
3. **Encryption Key**: `HKDF(SHA3-512, argonOutput, salt="encKeySalt", info="encKeyInfo")` -> 32-byte key
4. **Nonce**: `HKDF(SHA3-512, argonOutput, salt="nonceSalt", info="nonceInfo")` -> 16-byte nonce
5. **Auth Key**: `HKDF(SHA3-512, argonOutput, salt="authKeySalt", info="authKeyInfo")` -> 32-byte auth key
6. **Decrypt**: AES-256-CTR(key, nonce) -> ZIP content

### Custom Params Byte

The custom params byte encodes Argon2id parameters:
- Bits 0-3: memory multiplier (`m = 10240 * 2^mm`)
- Bits 4-7: time multiplier (`t = 10 * 2^tm`)

For `0x11` (binary `00010001`): mm=1, tm=1 -> m=20480 MB, t=20 iterations

## Building a Standalone JAR

To create a single executable JAR:

```bash
# Extract pfte.jar classes
mkdir -p tmp && cd tmp
jar xf ../lib/pfte.jar

# Copy wrapper class
cp ../out/DecryptPTE.class .

# Repackage
jar cfe ../lib/paranoia-decryptor.jar DecryptPTE DecryptPTE.class com/ sse/ META-INF/

# Run
java -Xmx24g -jar lib/paranoia-decryptor.jar input.enc output_dir password
```

## Limitations

- Requires the proprietary `pfte.jar` (not included due to copyright)
- Memory-intensive: Argon2id with m=20480 requires ~20GB RAM
- Only tested with algorithm code 0 (AES-256)

## Disclaimer

This tool is for legitimate file recovery purposes. Only decrypt files you have authorization to access.
