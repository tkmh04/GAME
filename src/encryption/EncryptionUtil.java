/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package encryption;

/**
 *
 * @author PC
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {
    private KeyPair rsaKeyPair; //cặp khóa RSA (khóa công khai và khóa riêng) từ Client
    private SecretKey aesKey; //khóa AES được sử dụng để mã hóa dữ liệu từ Server
    private PublicKey otherPublicKey; // Khóa công khai
    private final boolean isClient; // xác định

// Xác định vai trò (client hoặc server), thực hiện trao đổi khóa với đối tác qua các luồng I/O
    public EncryptionUtil(ObjectOutputStream out, ObjectInputStream in, boolean isClient) throws Exception {
        this.isClient = isClient;
        rsaKeyPair = generateRSAKeyPair();
        exchangeKeys(out, in);
    }
// tạo RSA
    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
/*
 * 
Client:

Gửi khóa công khai RSA của mình tới server.

Nhận khóa AES đã được mã hóa từ server (dưới dạng chuỗi Base64).

Giải mã khóa AES bằng khóa riêng RSA của mình.

Server:

Nhận khóa công khai RSA từ client.

Tạo khóa AES mới (256-bit).

Mã hóa khóa AES bằng khóa công khai RSA của client.

Gửi khóa AES đã mã hóa (chuỗi Base64) trở lại client.
 */

// trao đổi khoá
    private void exchangeKeys(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        if (isClient) {
            System.out.println("Client: Gửi khóa công khai RSA tới server");
            out.writeObject(rsaKeyPair.getPublic());
            out.flush();            
            Object received = in.readObject();
            if (!(received instanceof String)) {
                throw new IOException("Client: Nhận khóa AES đã được mã hóa từ server " + received.getClass().getName());
            }
            String encryptedAESKey = (String) received;
            System.out.println("Client: Giải mã khóa AES bằng khóa riêng RSA");
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] aesKeyBytes = rsaCipher.doFinal(Base64.getDecoder().decode(encryptedAESKey));
            aesKey = new SecretKeySpec(aesKeyBytes, "AES");              
        } else {           
            Object received = in.readObject();
            if (!(received instanceof PublicKey)) {
                throw new IOException("Server: Nhận khóa công khai RSA từ client " + received.getClass().getName());
            }
            otherPublicKey = (PublicKey) received;            
            System.out.println("Server: Tạo khóa AES");
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            aesKey = keyGen.generateKey();
            System.out.println("Server: Mã hóa khóa AES bằng khóa công khai RSA");
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
            String encodedAESKey = Base64.getEncoder().encodeToString(encryptedAESKey);
            System.out.println("Server: Gửi khóa AES mã hóa (Base64): " + encodedAESKey);
            out.writeObject(encodedAESKey);
            out.flush();
        }
    }
// Mã hóa dữ liệu
// trả về chuỗi Base64 đại diện cho dữ liệu mã hóa
    public String encrypt(Object data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(data);
        oos.close();
        byte[] plaintext = baos.toByteArray();
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        String encoded = Base64.getEncoder().encodeToString(combined);
        return encoded;
    }
// Giải mã dữ liệu đã được mã hóa (chuỗi Base64)
// khôi phục lại đối tượng gốc
    public Object decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu mã hóa không được null hoặc rỗng");
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            if (combined.length < 16) {
                throw new IllegalArgumentException("Dữ liệu mã hóa quá ngắn, không đủ IV");
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(combined, 16, combined.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            ByteArrayInputStream bais = new ByteArrayInputStream(plaintext);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object data = ois.readObject();
            ois.close();
            return data;
        } catch (Exception e) {
            throw e;
        }
    }
}