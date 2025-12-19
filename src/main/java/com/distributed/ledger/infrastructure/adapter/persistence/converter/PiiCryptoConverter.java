package com.distributed.ledger.infrastructure.adapter.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Converter
public class PiiCryptoConverter implements AttributeConverter<String, String> {

    // AES/GCM/NoPadding: Hem gizlilik hem de bütünlük (integrity) sağlar.
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // GCM Authentication Tag uzunluğu (16 byte)
    private static final int IV_LENGTH_BYTE = 12;  // GCM için önerilen IV uzunluğu (96 bit)
    private static final String KEY_ALGORITHM = "AES";

    private final SecretKey secretKeySpec;

    // Constructor Injection ile key'i güvenli hale getiriyoruz
    public PiiCryptoConverter(@Value("${security.encryption.key}") String rawKey) {
        try {
            // 1. Key'i temizle (trim) ve SHA-256 ile hashle
            // Bu işlem, girilen key ne kadar uzun veya hatalı olursa olsun
            // her zaman geçerli bir 32-byte (256-bit) AES anahtarı üretir.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawKey.trim().getBytes(StandardCharsets.UTF_8));

            // 2. Spec'i bir kere oluştur (Performans + Güvenlik)
            this.secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Encryption key initialization failed", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // 1. Her kayıt için benzersiz bir IV (Initialization Vector) üret
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            // 2. Cipher'ı hazırla (Daha önce oluşturduğumuz güvenli key ile)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            // 3. Şifrele
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // 4. IV ve Şifreli Veriyi birleştir
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // 5. Base64 olarak sakla
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting PII data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            // 1. Base64 decode
            byte[] decoded = Base64.getDecoder().decode(dbData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // 2. IV'yi ayır
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            // 3. Şifreli veriyi al
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // 4. Deşifre et
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error decrypting PII data", e);
        }
    }
}