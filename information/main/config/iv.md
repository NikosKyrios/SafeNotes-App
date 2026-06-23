- An Initialization Vector (IV) is a random or unique data block used in cryptography to add randomness to encryption, ensuring identical plaintexts encrypt to different ciphertexts, even with the same key, by providing an unpredictable starting point for algorithms like AES in modes like CBC or GCM, making patterns harder for attackers to spot. It's essential for security, especially in symmetric encryption, to avoid weaknesses where attackers could exploit repeated data, with common requirements being unpredictability and uniqueness, sometimes called a "nonce" (number used once).

# Key Functions & Characteristics

- Adds Randomness: Introduces unpredictability to the encryption process.
- Prevents Pattern Repetition: Ensures that if you encrypt the same message twice, you get two completely different encrypted versions.
- Used with a Key: Works alongside a secret key, but is not the key itself.
- Non-Secret: The IV is typically sent with the ciphertext (often unencrypted) because it's not secret; its unpredictability or uniqueness is what matters. 

# How it Works (Example: CBC Mode)

- First Block: The IV is combined (usually via XOR) with the first block of plaintext before encryption.
- Subsequent Blocks: Each subsequent plaintext block is XORed with the previous ciphertext block, with the IV effectively acting as the "previous block" for the very first one. 

# Why It's Important

- Security: Prevents attackers from analyzing repeated patterns in the ciphertext to deduce the plaintext, a vulnerability found in older systems like WEP.
- Modern Practice: Crucial for secure modes of operation (CBC, GCM, etc.) in block ciphers; using a predictable or reused IV can severely weaken encryption. 

# IV vs. Salt
- IV: Used in symmetric encryption (like AES) to initialize the encryption process.
- Salt: Used in password hashing (one-way functions) to make rainbow table attacks harder, often by concatenating to the password before hashing. 