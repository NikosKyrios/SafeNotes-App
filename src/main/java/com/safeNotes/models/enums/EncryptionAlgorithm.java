package com.safeNotes.models.enums;

public enum EncryptionAlgorithm {
    AES_256_GCM("AES/GCM/NoPadding", 256, 12, 128);

    private final String algorithm;
    private final int keySize;
    private final int ivLength;
    private final int tagLength;

    private EncryptionAlgorithm(String algorithm, int keySize, int ivLength, int tagLength) {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.ivLength = ivLength;
        this.tagLength = tagLength;
    }

    public String getAlgorithm() {return algorithm;}
    public int getKeySize() {return keySize;}
    public int getIVLength() {return ivLength;}
    public int getTagLength() {return tagLength;}
}
