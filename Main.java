import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        String matakuliahPath = "matakuliah.csv";
        String prasyaratPath = "prasyarat.csv";
        String studentPath = "mhs_01.txt";

        try {
            CourseCatalog catalog = CsvLoader.load(matakuliahPath, prasyaratPath);
            StudentData student = StudentFile.load(studentPath);

            Node[] available = EnrollmentChecker.getAvailableCourses(catalog, student);
            EnrollmentChecker.printAvailableCourses(student, available);

            System.out.println();
            UnavailableCourse[] unavailable = EnrollmentChecker.getUnavailableCourses(catalog, student);
            EnrollmentChecker.printUnavailableCourses(unavailable);
        } catch (Exception e) {
            System.err.println("Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class Node {
    String kode;
    String nama;
    int sks;
    int semester;
    Node prev;

    public Node(String kode, String nama, int sks, int semester) {
        this.kode = kode;
        this.nama = nama;
        this.sks = sks;
        this.semester = semester;
        this.prev = null;
    }
}

class UnavailableCourse {
    Node node;
    String reason;

    public UnavailableCourse(Node node, String reason) {
        this.node = node;
        this.reason = reason;
    }
}

class CourseCatalog {
    private final Node[] odd;
    private final Node[] even;
    private final String[] kodes;

    public CourseCatalog(Node[] odd, Node[] even, String[] kodes) {
        this.odd = odd;
        this.even = even;
        this.kodes = kodes;
    }

    public Node[] getOdd() {
        return odd;
    }

    public Node[] getEven() {
        return even;
    }

    public String[] getKodes() {
        return kodes;
    }

    public Node[] getNodesByKode(String kode) {
        Node nOdd = null;
        for (int i = 0; i < odd.length; i++) {
            if (odd[i].kode.equals(kode)) {
                nOdd = odd[i];
                break;
            }
        }
        Node nEven = null;
        for (int i = 0; i < even.length; i++) {
            if (even[i].kode.equals(kode)) {
                nEven = even[i];
                break;
            }
        }
        if (nOdd != null && nEven != null) {
            return new Node[] { nOdd, nEven };
        }
        if (nOdd != null) {
            return new Node[] { nOdd };
        }
        if (nEven != null) {
            return new Node[] { nEven };
        }
        return new Node[0];
    }
}

class CsvLoader {

    public static CourseCatalog load(String matakuliahPath, String prasyaratPath) throws IOException {
        BufferedReader br1 = Files.newBufferedReader(Paths.get(matakuliahPath));
        br1.readLine();

        int totalCourses = 0;
        int oddCount = 0;
        int evenCount = 0;
        String line;

        while ((line = br1.readLine()) != null) {
            String[] parts = line.split(";");
            String tipe = parts[4];
            totalCourses++;

            if (tipe.equals("Keduanya")) {
                oddCount++;
                evenCount++;
            } else if (tipe.equals("Ganjil")) {
                oddCount++;
            } else if (tipe.equals("Genap")) {
                evenCount++;
            }
        }
        br1.close();

        Node[] odd = new Node[oddCount];
        Node[] even = new Node[evenCount];
        String[] kodes = new String[totalCourses];

        BufferedReader br2 = Files.newBufferedReader(Paths.get(matakuliahPath));
        br2.readLine();

        int oddIdx = 0;
        int evenIdx = 0;
        int kodeIdx = 0;

        while ((line = br2.readLine()) != null) {
            String[] parts = line.split(";");
            String kode = parts[0];
            String nama = parts[1];
            int sks = Integer.parseInt(parts[2]);
            int semester = Integer.parseInt(parts[3]);
            String tipe = parts[4];

            kodes[kodeIdx++] = kode;

            if (tipe.equals("Keduanya")) {
                odd[oddIdx++] = new Node(kode, nama, sks, semester);
                even[evenIdx++] = new Node(kode, nama, sks, semester);
            } else if (tipe.equals("Ganjil")) {
                odd[oddIdx++] = new Node(kode, nama, sks, semester);
            } else if (tipe.equals("Genap")) {
                even[evenIdx++] = new Node(kode, nama, sks, semester);
            }
        }
        br2.close();

        CourseCatalog catalog = new CourseCatalog(odd, even, kodes);

        BufferedReader brPrasyarat = Files.newBufferedReader(Paths.get(prasyaratPath));
        brPrasyarat.readLine();

        while ((line = brPrasyarat.readLine()) != null) {
            String[] parts = line.split(";");
            String prasyaratKode = parts[0];
            String matakuliahKode = parts[1];

            Node[] prasyaratNodes = catalog.getNodesByKode(prasyaratKode);
            Node[] matakuliahNodes = catalog.getNodesByKode(matakuliahKode);

            Node prasyaratNode = prasyaratNodes[0];
            for (int i = 0; i < matakuliahNodes.length; i++) {
                matakuliahNodes[i].prev = prasyaratNode;
            }
        }
        brPrasyarat.close();

        return catalog;
    }
}

class StudentData {
    String semesterBerikutnya;
    String[] lulus;

    public StudentData(String semesterBerikutnya, String[] lulus) {
        this.semesterBerikutnya = semesterBerikutnya;
        this.lulus = lulus;
    }
}

class StudentFile {
    public static StudentData load(String path) throws IOException {
        BufferedReader br = Files.newBufferedReader(Paths.get(path));

        String line1 = br.readLine();
        String semesterBerikutnya = line1.split("=")[1];

        String[] lulus;
        String line2 = br.readLine();
        if (line2 != null && line2.contains("=")) {
            String[] parts = line2.split("=");
            if (parts.length > 1 && !parts[1].isEmpty()) {
                lulus = parts[1].split(",");
            } else {
                lulus = new String[0];
            }
        } else {
            lulus = new String[0];
        }
        br.close();

        return new StudentData(semesterBerikutnya, lulus);
    }
}

class EnrollmentChecker {
    private static boolean contains(String[] arr, int length, String target) {
        for (int i = 0; i < length; i++) {
            if (target.equals(arr[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOfferedInSemester(CourseCatalog catalog, String kode, String semesterBerikutnya) {
        Node[] targetArray = semesterBerikutnya.equals("ganjil") ? catalog.getOdd() : catalog.getEven();
        for (int i = 0; i < targetArray.length; i++) {
            if (targetArray[i].kode.equals(kode)) {
                return true;
            }
        }
        return false;
    }

    private static String getUnavailableReason(Node node, CourseCatalog catalog, StudentData student, String[] effectiveLulus, int effectiveCount) {
        if (node.kode.equals("SKR")) {
            String[] allKodes = catalog.getKodes();
            for (int i = 0; i < allKodes.length; i++) {
                if (!allKodes[i].equals("SKR") && !contains(effectiveLulus, effectiveCount, allKodes[i])) {
                    return "Belum menyelesaikan semua matkul";
                }
            }
            return null;
        }

        if (!isOfferedInSemester(catalog, node.kode, student.semesterBerikutnya)) {
            return "Tidak tersedia pada next semester";
        }

        if (node.prev != null && !contains(effectiveLulus, effectiveCount, node.prev.kode)) {
            return "Belum lulus prerequisite";
        }

        return null;
    }

    private static boolean isAvailable(Node node, CourseCatalog catalog, StudentData student, String[] effectiveLulus, int effectiveCount) {
        if (contains(effectiveLulus, effectiveCount, node.kode)) {
            return false;
        }
        return getUnavailableReason(node, catalog, student, effectiveLulus, effectiveCount) == null;
    }

    public static Node[] getAvailableCourses(CourseCatalog catalog, StudentData student) {
        int maxCourses = catalog.getKodes().length;
        String[] effectiveLulus = new String[maxCourses];
        int effectiveCount = 0;

        for (int i = 0; i < student.lulus.length; i++) {
            String k = student.lulus[i];
            if (!contains(effectiveLulus, effectiveCount, k)) {
                effectiveLulus[effectiveCount++] = k;
            }
        }

        for (int i = 0; i < student.lulus.length; i++) {
            Node[] nodes = catalog.getNodesByKode(student.lulus[i]);
            for (int j = 0; j < nodes.length; j++) {
                Node curr = nodes[j].prev;
                while (curr != null) {
                    if (!contains(effectiveLulus, effectiveCount, curr.kode)) {
                        effectiveLulus[effectiveCount++] = curr.kode;
                    }
                    curr = curr.prev;
                }
            }
        }

        Node[] semesterArray = student.semesterBerikutnya.equals("ganjil") ? catalog.getOdd() : catalog.getEven();

        int availableCount = 0;
        for (int i = 0; i < semesterArray.length; i++) {
            if (isAvailable(semesterArray[i], catalog, student, effectiveLulus, effectiveCount)) {
                availableCount++;
            }
        }

        Node[] available = new Node[availableCount];
        int idx = 0;
        for (int i = 0; i < semesterArray.length; i++) {
            if (isAvailable(semesterArray[i], catalog, student, effectiveLulus, effectiveCount)) {
                available[idx++] = semesterArray[i];
            }
        }

        return available;
    }

    public static UnavailableCourse[] getUnavailableCourses(CourseCatalog catalog, StudentData student) {
        int maxCourses = catalog.getKodes().length;
        String[] effectiveLulus = new String[maxCourses];
        int effectiveCount = 0;

        for (int i = 0; i < student.lulus.length; i++) {
            String k = student.lulus[i];
            if (!contains(effectiveLulus, effectiveCount, k)) {
                effectiveLulus[effectiveCount++] = k;
            }
        }

        for (int i = 0; i < student.lulus.length; i++) {
            Node[] nodes = catalog.getNodesByKode(student.lulus[i]);
            for (int j = 0; j < nodes.length; j++) {
                Node curr = nodes[j].prev;
                while (curr != null) {
                    if (!contains(effectiveLulus, effectiveCount, curr.kode)) {
                        effectiveLulus[effectiveCount++] = curr.kode;
                    }
                    curr = curr.prev;
                }
            }
        }

        String[] allKodes = catalog.getKodes();
        int count = 0;
        for (int i = 0; i < allKodes.length; i++) {
            String kode = allKodes[i];
            if (contains(effectiveLulus, effectiveCount, kode)) {
                continue;
            }
            Node node = catalog.getNodesByKode(kode)[0];
            String reason = getUnavailableReason(node, catalog, student, effectiveLulus, effectiveCount);
            if (reason != null) {
                count++;
            }
        }

        UnavailableCourse[] result = new UnavailableCourse[count];
        int idx = 0;
        for (int i = 0; i < allKodes.length; i++) {
            String kode = allKodes[i];
            if (contains(effectiveLulus, effectiveCount, kode)) {
                continue;
            }
            Node node = catalog.getNodesByKode(kode)[0];
            String reason = getUnavailableReason(node, catalog, student, effectiveLulus, effectiveCount);
            if (reason != null) {
                result[idx++] = new UnavailableCourse(node, reason);
            }
        }
        return result;
    }

    public static void printAvailableCourses(StudentData student, Node[] available) {
        System.out.println("Semester berikutnya: " + student.semesterBerikutnya);
        System.out.println("Matakuliah yang dapat diambil (" + available.length + "):");

        int totalSks = 0;
        for (int i = 0; i < available.length; i++) {
            Node n = available[i];
            totalSks += n.sks;
            System.out.printf("%2d. %-6s %-42s %d sks  (smt %d)\n",
                    (i + 1), n.kode, n.nama, n.sks, n.semester);
        }
        System.out.println("Total: " + totalSks + " sks");
    }

    public static void printUnavailableCourses(UnavailableCourse[] unavailable) {
        System.out.println("Matakuliah yang tidak dapat diambil (" + unavailable.length + "):");
        for (int i = 0; i < unavailable.length; i++) {
            UnavailableCourse u = unavailable[i];
            System.out.printf("%2d. %-6s %-42s %d sks  (smt %d) - %s\n",
                    (i + 1), u.node.kode, u.node.nama, u.node.sks, u.node.semester, u.reason);
        }
    }
}
