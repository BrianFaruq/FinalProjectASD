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

class CourseCatalog {
    private final Node[] odd;
    private final Node[] even;
    private final String[] kodes;
    private final int[][] kodeToIndices;

    public CourseCatalog(Node[] odd, Node[] even, String[] kodes, int[][] kodeToIndices) {
        this.odd = odd;
        this.even = even;
        this.kodes = kodes;
        this.kodeToIndices = kodeToIndices;
    }

    public Node[] getOdd() {
        return odd;
    }

    public Node[] getEven() {
        return even;
    }

    public int getTotalNodes() {
        return odd.length + even.length;
    }

    public Node getNodeByGlobalIndex(int idx) {
        if (idx < odd.length) {
            return odd[idx];
        } else {
            return even[idx - odd.length];
        }
    }

    public int[] getGlobalIndices(String kode) {
        for (int i = 0; i < kodes.length; i++) {
            if (kode.equals(kodes[i])) {
                if (kodeToIndices[i][1] != -1) {
                    return new int[] { kodeToIndices[i][0], kodeToIndices[i][1] };
                }
                return new int[] { kodeToIndices[i][0] };
            }
        }
        return new int[0];
    }

    public Node[] getNodesByKode(String kode) {
        int[] indices = getGlobalIndices(kode);
        Node[] nodes = new Node[indices.length];
        for (int i = 0; i < indices.length; i++) {
            nodes[i] = getNodeByGlobalIndex(indices[i]);
        }
        return nodes;
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
        int[][] kodeToIndices = new int[totalCourses][2];
        for (int i = 0; i < totalCourses; i++) {
            kodeToIndices[i][0] = -1;
            kodeToIndices[i][1] = -1;
        }

        BufferedReader br2 = Files.newBufferedReader(Paths.get(matakuliahPath));
        br2.readLine();

        int oddIdx = 0;
        int evenIdx = 0;

        while ((line = br2.readLine()) != null) {
            String[] parts = line.split(";");
            String kode = parts[0];
            String nama = parts[1];
            int sks = Integer.parseInt(parts[2]);
            int semester = Integer.parseInt(parts[3]);
            String tipe = parts[4];

            if (tipe.equals("Keduanya")) {
                odd[oddIdx] = new Node(kode, nama, sks, semester);
                registerKodeIndex(kodes, kodeToIndices, kode, oddIdx++);

                even[evenIdx] = new Node(kode, nama, sks, semester);
                registerKodeIndex(kodes, kodeToIndices, kode, oddCount + evenIdx++);
            } else if (tipe.equals("Ganjil")) {
                odd[oddIdx] = new Node(kode, nama, sks, semester);
                registerKodeIndex(kodes, kodeToIndices, kode, oddIdx++);
            } else if (tipe.equals("Genap")) {
                even[evenIdx] = new Node(kode, nama, sks, semester);
                registerKodeIndex(kodes, kodeToIndices, kode, oddCount + evenIdx++);
            }
        }
        br2.close();

        CourseCatalog catalog = new CourseCatalog(odd, even, kodes, kodeToIndices);

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

    private static void registerKodeIndex(String[] kodes, int[][] kodeToIndices, String kode, int globalIdx) {
        for (int i = 0; i < kodes.length; i++) {
            if (kode.equals(kodes[i])) {
                kodeToIndices[i][1] = globalIdx;
                return;
            } else if (kodes[i] == null) {
                kodes[i] = kode;
                kodeToIndices[i][0] = globalIdx;
                return;
            }
        }
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

    public static Node[] getAvailableCourses(CourseCatalog catalog, StudentData student) {
        int maxCourses = catalog.getTotalNodes();
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
            Node node = semesterArray[i];
            if (contains(effectiveLulus, effectiveCount, node.kode)) {
                continue;
            }
            if (node.prev != null && !contains(effectiveLulus, effectiveCount, node.prev.kode)) {
                continue;
            }
            availableCount++;
        }

        Node[] available = new Node[availableCount];
        int idx = 0;
        for (int i = 0; i < semesterArray.length; i++) {
            Node node = semesterArray[i];
            if (contains(effectiveLulus, effectiveCount, node.kode)) {
                continue;
            }
            if (node.prev != null && !contains(effectiveLulus, effectiveCount, node.prev.kode)) {
                continue;
            }
            available[idx++] = node;
        }

        return available;
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
}
