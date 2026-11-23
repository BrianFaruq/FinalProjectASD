import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String matakuliahPath = "matakuliah.csv";
        String prasyaratPath = "prasyarat.csv";
        String studentPath = "mhs_01.txt";

        try {
            CourseCatalog catalog = CsvLoader.load(matakuliahPath, prasyaratPath);
            StudentData student = StudentFile.load(studentPath);
            List<Node> available = EnrollmentChecker.getAvailableCourses(catalog, student);
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
    Node prev; // Pointer ke matakuliah prasyarat, null jika tidak ada

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
    private int[][] adj;

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

    public void setAdj(int[][] adj) {
        this.adj = adj;
    }

    public int[][] getAdj() {
        return adj;
    }
}

class CsvLoader {

    public static CourseCatalog load(String matakuliahPath, String prasyaratPath) throws IOException {
        // Pass 1: Hitung jumlah baris/kategori
        BufferedReader br1 = Files.newBufferedReader(Paths.get(matakuliahPath));
        br1.readLine(); // Lewati header: Kode;Nama;sks;semester;ganjil/genap

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

        // Alokasi array sesuai ukuran eksak
        Node[] odd = new Node[oddCount];
        Node[] even = new Node[evenCount];
        String[] kodes = new String[totalCourses];
        int[][] kodeToIndices = new int[totalCourses][2];
        for (int i = 0; i < totalCourses; i++) {
            kodeToIndices[i][0] = -1;
            kodeToIndices[i][1] = -1;
        }

        // Pass 2: Isi data matakuliah
        BufferedReader br2 = Files.newBufferedReader(Paths.get(matakuliahPath));
        br2.readLine(); // Lewati header

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

        // Pass 3: Parse prasyarat.csv
        int totalNodes = catalog.getTotalNodes();
        @SuppressWarnings("unchecked")
        List<Integer>[] adjList = new List[totalNodes];
        for (int i = 0; i < totalNodes; i++) {
            adjList[i] = new ArrayList<>();
        }

        BufferedReader br3 = Files.newBufferedReader(Paths.get(prasyaratPath));
        br3.readLine(); // Lewati header: prasyarat;matakuliah

        while ((line = br3.readLine()) != null) {
            String[] parts = line.split(";");
            String prasyaratKode = parts[0];
            String matakuliahKode = parts[1];

            Node prasyaratNode = catalog.getNodesByKode(prasyaratKode)[0];
            for (Node mkNode : catalog.getNodesByKode(matakuliahKode)) {
                mkNode.prev = prasyaratNode;
            }

            int[] srcIndices = catalog.getGlobalIndices(prasyaratKode);
            int[] dstIndices = catalog.getGlobalIndices(matakuliahKode);
            for (int src : srcIndices) {
                for (int dst : dstIndices) {
                    adjList[src].add(dst);
                }
            }
        }
        br3.close();

        int[][] adj = new int[totalNodes][];
        for (int i = 0; i < totalNodes; i++) {
            adj[i] = new int[adjList[i].size()];
            for (int j = 0; j < adjList[i].size(); j++) {
                adj[i][j] = adjList[i].get(j);
            }
        }
        catalog.setAdj(adj);

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
    Set<String> lulus;

    public StudentData(String semesterBerikutnya, Set<String> lulus) {
        this.semesterBerikutnya = semesterBerikutnya;
        this.lulus = lulus;
    }
}

class StudentFile {
    public static StudentData load(String path) throws IOException {
        BufferedReader br = Files.newBufferedReader(Paths.get(path));

        // Format terjamin:
        // Baris 1: semester_berikutnya=...
        // Baris 2: lulus=...
        String line1 = br.readLine();
        String semesterBerikutnya = line1.split("=")[1];

        Set<String> lulus = new HashSet<>();
        String line2 = br.readLine();
        if (line2 != null && line2.contains("=")) {
            String[] parts = line2.split("=");
            if (parts.length > 1 && !parts[1].isEmpty()) {
                String[] courses = parts[1].split(",");
                for (String c : courses) {
                    lulus.add(c);
                }
            }
        }
        br.close();

        return new StudentData(semesterBerikutnya, lulus);
    }
}

class EnrollmentChecker {
    public static List<Node> getAvailableCourses(CourseCatalog catalog, StudentData student) {
        Set<String> effectiveLulus = new HashSet<>(student.lulus);

        // Menelusuri seluruh rantai SLL ke belakang (prev hingga null)
        for (String kodeLulus : student.lulus) {
            for (Node n : catalog.getNodesByKode(kodeLulus)) {
                Node curr = n.prev;
                while (curr != null) {
                    effectiveLulus.add(curr.kode);
                    curr = curr.prev;
                }
            }
        }

        Node[] array = student.semesterBerikutnya.equals("ganjil") ? catalog.getOdd() : catalog.getEven();
        List<Node> available = new ArrayList<>();

        for (Node node : array) {
            // Sudah lulus atau prasyaratnya terpenuhi dalam rantai -> omit
            if (effectiveLulus.contains(node.kode)) {
                continue;
            }
            // Jika memiliki prasyarat, pastikan sudah terpenuhi
            if (node.prev != null && !effectiveLulus.contains(node.prev.kode)) {
                continue;
            }
            available.add(node);
        }

        return available;
    }

    public static void printAvailableCourses(StudentData student, List<Node> available) {
        System.out.println("Semester berikutnya: " + student.semesterBerikutnya);
        System.out.println("Matakuliah yang dapat diambil (" + available.size() + "):");

        int totalSks = 0;
        for (int i = 0; i < available.size(); i++) {
            Node n = available.get(i);
            totalSks += n.sks;
            System.out.printf("%2d. %-6s %-42s %d sks  (smt %d)\n",
                    (i + 1), n.kode, n.nama, n.sks, n.semester);
        }
        System.out.println("Total: " + totalSks + " sks");
    }
}
