import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

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
    switch (command) {
    case "keygen":
      main.keygenerate();
      System.out.println("key pair generated");
      break;
    case "encrypt":
      main.encrypt();
      break;
    case "add":
      main.add();
    default:
      System.out.println("Invalid command: " + args[0]);
    }
  }

  private void add() {
  }

  private void encrypt() {
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

  // private boolean exists() {
  // return new File(pubPath).exists() && new File(pvtPath).exists();
  // }
}