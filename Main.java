import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class Main {
  String homeDirectory = System.getProperty("user.home");
  String pubPath = homeDirectory + "\\.it\\public.key";
  String pvtPath = homeDirectory + "\\.it\\private.key";

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("No command given");
      return;
    }
    String command = args[0];
    Main main = new Main();
    String name;

    switch (command) {
    case "keygen":
      main.keygenerate();
      System.out.println("key pair generated");
      break;
    case "add":
      name = args[1];
      String filePath = args[2];
      main.add(name, filePath);
      System.out.println("Public Key added of " + name);
      break;
    case "encrypt":
      if (args.length < 3) {
        System.out.println("Incomplete command");
        return;
      }
      if (!main.existsEncrypt(args[1], args[2])) {
        System.out.println("Invalid arguments");
        return;
      }
      main.encrypt(args[1], args[2]);
      System.out.println("Encrypted");
      break;
    case "decrypt":
      if (args.length < 3) {
        System.out.println("Incomeplete command");
        return;
      }
      if (!main.existsDecrypt(args[1], args[2])) {
        System.out.println("Invalid arguments or no private key exists");
        return;
      }
      main.decrypt(args[1], args[2]);
      System.out.println("Decrypted");
      break;
    case "help":
      System.out.println("Hybrid Encryption");
      System.out.println("Commands: ");
      System.out.println("keygen: Generates public/private key pair");
      System.out.println("add <name> <public key filepath>: Add public key of other users");
      System.out.println(
          "encrypt <name> <message file path>: Encrypt message using public key of other user. Generates two files - sessionkey and cipher");
      System.out.println(
          "decrypt <session key file path> <cipher file path>: Encrypt message using public key of other user. Generates two files - sessionkey and cipher");
      break;
    default:
      System.out.println("Invalid command: " + args[0]);
    }
  }

  private void decrypt(String sessionKeyPath, String cipherTextPath)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    File privateKeyFile = new File(pvtPath);
    byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    Cipher decryptCipher = Cipher.getInstance("RSA");
    decryptCipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(privateKeySpec));
    File sessionFile = new File(sessionKeyPath);
    byte[] encryptedSessionKeyBytes = Files.readAllBytes(sessionFile.toPath());
    byte[] decryptedSessionKeyBytes = decryptCipher.doFinal(encryptedSessionKeyBytes);
    SecretKey key = new SecretKeySpec(decryptedSessionKeyBytes, "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, key);
    FileInputStream inputStream = new FileInputStream(cipherTextPath);
    FileOutputStream outputStream = new FileOutputStream("decipher");
    byte[] buffer = new byte[64];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      byte[] output = cipher.update(buffer, 0, bytesRead);
      if (output != null) {
        outputStream.write(output);
      }
    }
    byte[] outputBytes = cipher.doFinal();
    if (outputBytes != null) {
      outputStream.write(outputBytes);
    }
    inputStream.close();
    outputStream.close();
  }

  private void add(String name, String filePath) {
    try {
      File file1 = new File(filePath);
      File file2 = new File(homeDirectory + "\\.it\\" + name);
      FileInputStream fin = new FileInputStream(file1);
      FileOutputStream fos = new FileOutputStream(file2);
      int n;
      while ((n = fin.read()) != -1) {
        fos.write(n);
      }
      fin.close();
      fos.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void encrypt(String name, String messagePath) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(128);
    SecretKey key = keyGenerator.generateKey();
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    FileInputStream inputStream = new FileInputStream(messagePath);
    FileOutputStream outputStream = new FileOutputStream("cipher");
    byte[] buffer = new byte[64];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      byte[] output = cipher.update(buffer, 0, bytesRead);
      if (output != null) {
        outputStream.write(output);
      }
    }
    byte[] outputBytes = cipher.doFinal();
    if (outputBytes != null) {
      outputStream.write(outputBytes);
    }
    inputStream.close();
    outputStream.close();
    File publicKeyFile = new File(homeDirectory + "\\.it\\" + name);
    byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    Cipher encryptCipher = Cipher.getInstance("RSA");
    encryptCipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(publicKeySpec));
    byte[] encryptedMessageBytes = encryptCipher.doFinal(key.getEncoded());
    FileOutputStream fos = new FileOutputStream("sessionkey");
    fos.write(encryptedMessageBytes);
    fos.close();
  }

  private void keygenerate() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    KeyPair pair = generator.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();
    try {
      File directory = new File(homeDirectory + "\\.it");
      if (!directory.exists()) {
        directory.mkdir();
      }
      File f1 = new File(pubPath);
      FileOutputStream fos1 = new FileOutputStream(f1);
      fos1.write(publicKey.getEncoded());
      File f2 = new File(pvtPath);
      FileOutputStream fos2 = new FileOutputStream(f2);
      fos2.write(privateKey.getEncoded());
      fos1.close();
      fos2.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private boolean existsEncrypt(String name, String messagePath) {
    return new File(homeDirectory + "\\.it\\" + name).exists() && new File(messagePath).exists();
  }

  private boolean existsDecrypt(String sessionKeyPath, String cipherTextPath) {
    return new File(pvtPath).exists() && new File(sessionKeyPath).exists() && new File(cipherTextPath).exists();
  }
}