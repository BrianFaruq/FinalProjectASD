# Course Enrollment Availability Checker (Sistem Rekomendasi Rencana Studi)

Sistem penentu ketersediaan mata kuliah (*Course Enrollment Checker*) berbasis **Algoritma dan Struktur Data (ASD)**. Program ini menganalisis katalog mata kuliah kurikulum, graf ketergantungan prasyarat, serta rekam jejak akademik mahasiswa untuk menentukan mata kuliah apa saja yang memenuhi syarat (*eligible*) untuk diambil pada semester berikutnya.

Proyek ini dibangun secara khusus dengan paradigma **murni array standar Java** (tanpa pustaka koleksi generik seperti `ArrayList`, `HashSet`, `HashMap`), menggunakan struktur data *Singly Linked List* (SLL) dan graf *Adjacency List* 2D array.

---

## 📖 Gambaran Umum (Broad Overview)

Dalam sistem akademik perguruan tinggi, mahasiswa mengambil mata kuliah setiap semester dengan mematuhi dua aturan utama:
1. **Periodisitas Semester**: Mata kuliah tertentu hanya ditawarkan pada semester tertentu (Ganjil atau Genap), kecuali mata kuliah khusus (misalnya Skripsi) yang ditawarkan pada **Keduanya**.
2. **Ketergantungan Prasyarat (*Prerequisites*)**: Mata kuliah tingkat lanjut mensyaratkan kelulusan mata kuliah pendahulu (misal: *Pemrograman Lanjut* mensyaratkan *Pemrograman Dasar*).

### Masukan Program (*Inputs*)
Program membaca 3 berkas data yang berada dalam direktori yang sama:
- **`matakuliah.csv`**: Katalog seluruh mata kuliah dengan format:
  ```text
  Kode;Nama;sks;semester;ganjil/genap
  RPL;Rekayasa Perangkat Lunak;4;1;Ganjil
  PLAN;Pemrograman Lanjut;5;2;Genap
  SKR;Skripsi;6;8;Keduanya
  ```
- **`prasyarat.csv`**: Relasi ketergantungan graf prasyarat dengan format:
  ```text
  prasyarat;matakuliah
  PDAS;PLAN
  PLAN;ASD
  ASD;DAA
  ```
- **`mhs_01.txt`**: Data mahasiswa target dengan format:
  ```text
  semester_berikutnya=ganjil
  lulus=APS,DAA,JK,IMK,KA,KALJ
  ```

### Aturan Bisnis & Logika Akademik
1. **Filter Paritas Semester**: Hanya mata kuliah yang sesuai dengan `semester_berikutnya` (atau bertipe `Keduanya`) yang dipertimbangkan.
2. **Eliminasi Mata Kuliah yang Sudah Lulus**: Mata kuliah yang sudah pernah diselesaikan tidak akan ditawarkan kembali.
3. **Penelusuran Rantai Prasyarat Mundur (*Backward SLL Traversal*)**:
   - Jika mahasiswa telah lulus mata kuliah lanjutan (misalnya `DAA`), sistem mengasumsikan seluruh rantai prasyarat pendahulunya (`ASD` $\rightarrow$ `PLAN` $\rightarrow$ `PDAS`) **sudah pasti terpenuhi**.
   - Oleh karena itu, seluruh mata kuliah dalam rantai prasyarat tersebut otomatis di-*omit* (dieliminasi) agar tidak ditawarkan kembali.
4. **Verifikasi Prasyarat Langsung**: Mata kuliah baru hanya ditawarkan apabila prasyarat langsungnya (`node.prev`) sudah berstatus lulus atau terpenuhi dalam rantai prasyarat.

---

## 🛠️ Penjelasan Teknis (Technical Details)

Program diimplementasikan dalam satu berkas mandiri (`Main.java`) dengan pemisahan tanggung jawab antarkelas:

```text
Main.java
├── public class Main        // Titik masuk program (wiring & eksekusi)
├── class Node               // Node SLL mata kuliah (pointer prev ke prasyarat)
├── class CourseCatalog      // Penyimpan array Ganjil/Genap, lookup tabel, dan graf
├── class CsvLoader          // Parser CSV dengan teknik Two-Pass Scanning
├── class StudentData        // Struktur data penampung info mahasiswa
├── class StudentFile        // Parser file mahasiswa mhs_XX.txt
└── class EnrollmentChecker  // Logika backward traversal dan penyaringan ketersediaan
```

### 1. Kepatuhan Bebas Koleksi Java (*No Java Collections / Generics*)
Untuk memenuhi kaidah murni Struktur Data:
- ❌ Tidak menggunakan `java.util.List`, `java.util.ArrayList`
- ❌ Tidak menggunakan `java.util.Set`, `java.util.HashSet`
- ❌ Tidak menggunakan `java.util.Map`, `java.util.HashMap`
- ❌ Tidak ada tipe parameter generik `<T>`
- ✅ Murni menggunakan **Array Primitif & Array Objek Standar**: `Node[]`, `String[]`, `int[]`, `int[][]`, serta pointer objek.

### 2. Representasi Struktur Data
- **Node Singly Linked List (SLL)**:
  ```java
  class Node {
      String kode;
      String nama;
      int sks;
      int semester;
      Node prev; // Menunjuk ke mata kuliah prasyarat langsung (mundur hingga null)
  }
  ```
- **Katalog Terpisah (Paritas Semester)**:
  - `Node[] odd`: Menampung mata kuliah semester ganjil.
  - `Node[] even`: Menampung mata kuliah semester genap.
  - Mata kuliah `Keduanya` (misal `SKR`) diinstansiasi menjadi 2 node identik: satu masuk ke `odd`, satu masuk ke `even`.
- **Tabel Pemetaan Kode ke Index 2D Array (`kodeToIndices`)**:
  - `String[] kodes`: Menyimpan kode unik mata kuliah.
  - `int[][] kodeToIndices`: Array 2D berukuran $N \times 2$. Kolom 0 menyimpan indeks global pertama, dan kolom 1 menyimpan indeks kedua jika mata kuliah bertipe `Keduanya`.
- **Graf Prasyarat (*Jagged 2D Array*)**:
  - `int[][] adj`: Representasi graf berarah (*directed graph*) `prasyarat -> matakuliah`. Setiap baris `adj[i]` dialokasikan persis sesuai *out-degree* dari simpul `i`.

### 3. Teknik *Two-Pass Scanning* (Alokasi Ukuran Eksak)
Untuk menghindari alokasi memori dinamis (`ArrayList`) atau *hardcoded capacity*, pembacaan file dilakukan dalam 2 putaran (*two-pass*):
1. **Pass 1 (`matakuliah.csv`)**: Memindai file untuk menghitung jumlah total mata kuliah unik, jumlah ganjil, dan jumlah genap.
2. **Alokasi**: Mengalokasikan `Node[] odd`, `Node[] even`, `String[] kodes`, dan `int[][] kodeToIndices` dengan ukuran 100% presisi (*exact sizing*).
3. **Pass 2 (`matakuliah.csv`)**: Membaca ulang berkas untuk membuat objek `Node` dan mengisi tabel pemetaan.
4. **Pass 3 (`prasyarat.csv`)**: Menghitung derajat keluar (*out-degree*) tiap node.
5. **Pass 4 (`prasyarat.csv`)**: Mengalokasikan array 2D `adj` dan menghubungkan pointer `node.prev` ke node prasyaratnya.

### 4. Algoritma Pengecekan Ketersediaan (*Enrollment Checker*)

```mermaid
flowchart TD
    Start([Mulai]) --> ReadStudent[Baca Data Mahasiswa]
    ReadStudent --> CollectPassed[Kumpulkan Matakuliah Lulus ke effectiveLulus]
    CollectPassed --> BackwardTraversal[Untuk setiap matakuliah lulus:<br/>Telusuri pointer prev mundur hingga null]
    BackwardTraversal --> MarkAncestors[Tandai seluruh rantai prasyarat sebagai terpenuhi]
    MarkAncestors --> SelectArray{semester_berikutnya?}
    SelectArray -->|ganjil| UseOdd[Pilih odd array]
    SelectArray -->|genap| UseEven[Pilih even array]
    UseOdd --> ScanLoop[Loop setiap Node pada array terpilih]
    UseEven --> ScanLoop
    ScanLoop --> CheckPassed{Ada di effectiveLulus?}
    CheckPassed -->|Ya| SkipCourse[Lewati / Omit]
    CheckPassed -->|Tidak| CheckPrereq{Punya prasyarat prev?}
    CheckPrereq -->|Tidak| AddAvail[Masukkan ke Available]
    CheckPrereq -->|Ya| PrereqMet{prev.kode ada di effectiveLulus?}
    PrereqMet -->|Ya| AddAvail
    PrereqMet -->|Tidak| SkipCourse
    SkipCourse --> NextNode{Masih ada node?}
    AddAvail --> NextNode
    NextNode -->|Ya| ScanLoop
    NextNode -->|Tidak| PrintResult[Cetak Daftar Matakuliah & Total SKS]
    PrintResult --> End([Selesai])
```

---

## 🚀 Panduan Kompilasi & Menjalankan

### Prasyarat
- Java Development Kit (JDK) versi 8 atau yang lebih baru.

### Langkah-langkah
Buka terminal / PowerShell dan arahkan ke direktori proyek:

```bash
cd D:\Sekul\ASD\FinalProjectASD
```

Kompilasi kode program:
```bash
javac Main.java
```

Jalankan program:
```bash
java Main
```

---

## 📊 Contoh Hasil Eksekusi

Dengan data mahasiswa pada [mhs_01.txt](file:///D:/Sekul/ASD/FinalProjectASD/mhs_01.txt):
```text
semester_berikutnya=ganjil
lulus=APS,DAA,JK,IMK,KA,KALJ
```

**Output Terminal:**
```text
Semester berikutnya: ganjil
Matakuliah yang dapat diambil (13):
 1. PKK    Pengenalan Keilmuan Komputer               2 sks  (smt 1)
 2. STP    Statistik dan Teori Peluang                2 sks  (smt 1)
 3. AGM    Agama                                      2 sks  (smt 1)
 4. MN     Metode Numerik                             3 sks  (smt 3)
 5. PAPB   Pengembangan Aplikasi Perangkat Bergerak   3 sks  (smt 5)
 6. JN     Jaringan Nirkabel                          3 sks  (smt 5)
 7. KG     Komputer Grafis                            2 sks  (smt 5)
 8. JST    Jaringan Saraf Tiruan                      3 sks  (smt 5)
 9. SI     Statistika Inferensi                       2 sks  (smt 5)
10. MPPI   Metodologi Penelitian dan Penulisan Ilmiah 3 sks  (smt 5)
11. BIND   Bahasa Indonesia                           2 sks  (smt 5)
12. PKL    Praktik Kerja Lapangan                     4 sks  (smt 7)
13. SKR    Skripsi                                    6 sks  (smt 8)
Total: 37 sks
```

### Penjelasan Kasus Uji:
- Mahasiswa telah lulus `DAA`, `APS`, `JK`, `IMK`, `KA`, `KALJ`.
- Rantai mundur prasyarat yang otomatis terpenuhi & di-*omit*:
  - `APS` $\rightarrow$ `RPL` di-*omit*.
  - `DAA` $\rightarrow$ `ASD` $\rightarrow$ `PLAN` $\rightarrow$ `PDAS` semuanya di-*omit*.
  - `JK` & `IMK` $\rightarrow$ `SO` $\rightarrow$ `AOK` semuanya di-*omit*.
  - `KALJ` & `KA` $\rightarrow$ `AL` $\rightarrow$ `KAL` semuanya di-*omit*.
- Matakuliah semester 1 yang ditawarkan murni hanya yang belum pernah diambil dan tidak masuk rantai prasyarat di atas (`PKK`, `STP`, `AGM`).
- Matakuliah semester 3 seperti `MN` ditawarkan karena prasyaratnya (`AL`) telah terpenuhi lewat kelulusan `KA`/`KALJ`.
