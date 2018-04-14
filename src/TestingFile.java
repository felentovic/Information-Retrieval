import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestingFile {

    public static void main(String[] args)  {
//        HashMap<String, List<String>> indexMap = null;
//        try {
//            FileInputStream streamIn = new FileInputStream("/home/felentovic/tmp/index0.sr");
//            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
//            indexMap = (HashMap<String, List<String>>) objectinputstream.readObject();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(readCase);

        System.out.println(Arrays.asList("ovo je test neki:\"probavam\" kako ide 23$ proa je 2<3*4".split("[\\s+\":\\t+{}|~^\\]\\[\\?\\!<>=;:,\\+\\-\\*\\(\\)\\%]")));

    }
}
