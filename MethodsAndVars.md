# Dokumentasi Metode dan Variabel (Methods and Variables Documentation)

**Proyek:** Course Enrollment Availability Checker (Sistem Rekomendasi Rencana Studi)  
**Mata Kuliah:** Algoritma dan Struktur Data (ASD)  
**Penyusun:** Brilliant Faruq Wicaksono  
**NIM:** 255150200111033  

---

## Daftar Isi
1. [Class: Main](#1-class-main)
2. [Class: Node](#2-class-node)
3. [Class: UnavailableCourse](#3-class-unavailablecourse)
4. [Class: CourseCatalog](#4-class-coursecatalog)
5. [Class: CsvLoader](#5-class-csvloader)
6. [Class: StudentData](#6-class-studentdata)
7. [Class: StudentFile](#7-class-studentfile)
8. [Class: EnrollmentChecker](#8-class-enrollmentchecker)

---

## 1. Class: Main

Kelas utama yang menjadi titik masuk (*entry point*) eksekusi program. Kelas ini bertanggung jawab atas inisialisasi path berkas, pemanggilan parser data, pemanggilan pemeriksa ketersediaan mata kuliah yang dapat diambil maupun yang tidak dapat diambil beserta alasannya, serta penanganan eksepsi global.

### Metode

#### `public static void main(String[] args)`
- **Fungsi:** Titik masuk eksekusi program Java. Mengatur alur kerja secara berurutan:
  1. Menentukan path berkas data (`matakuliah.csv`, `prasyarat.csv`, `mhs_01.txt`).
  2. Memuat katalog kurikulum dan menghubungkan prasyarat via `CsvLoader.load()`.
  3. Memuat profil mahasiswa via `StudentFile.load()`.
  4. Menyaring mata kuliah yang memenuhi syarat via `EnrollmentChecker.getAvailableCourses()`.
  5. Mencetak daftar mata kuliah yang dapat diambil via `EnrollmentChecker.printAvailableCourses()`.
  6. Menyaring mata kuliah yang tidak dapat diambil beserta alasannya via `EnrollmentChecker.getUnavailableCourses()`.
  7. Mencetak daftar mata kuliah yang tidak dapat diambil via `EnrollmentChecker.printUnavailableCourses()`.
- **Parameter:**
  - `args` (`String[]`): Argumen command-line (tidak digunakan karena path diakses secara relatif).
- **Variabel Lokal:**
  - `matakuliahPath` (`String`): Menyimpan path relatif berkas katalog mata kuliah (`"matakuliah.csv"`).
  - `prasyaratPath` (`String`): Menyimpan path relatif berkas graf ketergantungan prasyarat (`"prasyarat.csv"`).
  - `studentPath` (`String`): Menyimpan path relatif berkas profil kelulusan mahasiswa (`"mhs_01.txt"`).
  - `catalog` (`CourseCatalog`): Objek penampung katalog mata kuliah hasil parsing.
  - `student` (`StudentData`): Objek penampung data mahasiswa hasil parsing.
  - `available` (`Node[]`): Array berukuran eksak yang menampung daftar node mata kuliah yang dapat diambil mahasiswa.
  - `unavailable` (`UnavailableCourse[]`): Array berukuran eksak yang menampung daftar mata kuliah yang tidak dapat diambil beserta alasan penolakannya.
  - `e` (`Exception`): Objek penangkap eksepsi jika terjadi kesalahan pembacaan berkas atau parsing.

---

## 2. Class: Node

Merepresentasikan simpul (*node*) individual dari suatu mata kuliah dalam struktur data **Branched Singly Linked List (SLL)**.

### Variabel / Atribut Kelas (Fields)

- `kode` (`String`): Kode unik identitas mata kuliah (misal: `"RPL"`, `"PDAS"`, `"SKR"`).
- `nama` (`String`): Nama lengkap mata kuliah (misal: `"Rekayasa Perangkat Lunak"`).
- `sks` (`int`): Bobot Satuan Kredit Semester dari mata kuliah.
- `semester` (`int`): Rekomendasi tingkat semester kurikulum tempat mata kuliah diajarkan (1–8).
- `prev` (`Node`): Pointer berarah mundur yang menunjuk langsung ke simpul mata kuliah prasyaratnya dalam rantai Singly Linked List. Bernilai `null` apabila mata kuliah tidak memiliki prasyarat.

### Metode

#### `public Node(String kode, String nama, int sks, int semester)`
- **Fungsi:** Konstruktor untuk menginisialisasi objek `Node` baru dengan atribut dasar mata kuliah serta mengatur pointer `prev = null`.
- **Parameter:**
  - `kode` (`String`): Kode mata kuliah.
  - `nama` (`String`): Nama lengkap mata kuliah.
  - `sks` (`int`): Jumlah bobot SKS.
  - `semester` (`int`): Semester kurikulum.

---

## 3. Class: UnavailableCourse

Merepresentasikan pasangan antara simpul mata kuliah yang tidak dapat diambil dengan deskripsi alasan mengapa mata kuliah tersebut tidak dapat diambil.

### Variabel / Atribut Kelas (Fields)

- `node` (`Node`): Referensi simpul mata kuliah yang tidak dapat diambil.
- `reason` (`String`): Alasan penolakan pengambilan mata kuliah (misal: `"Tidak tersedia pada next semester"`, `"Belum lulus prerequisite"`, `"Belum menyelesaikan semua matkul"`).

### Metode

#### `public UnavailableCourse(Node node, String reason)`
- **Fungsi:** Konstruktor untuk menginisialisasi objek mata kuliah yang tidak dapat diambil beserta alasannya.
- **Parameter:**
  - `node` (`Node`): Simpul mata kuliah terkait.
  - `reason` (`String`): String alasan penolakan.

---

## 4. Class: CourseCatalog

Mengelola keseluruhan katalog mata kuliah, pemisahan paritas semester ganjil/genap, array daftar seluruh kode kurikulum, serta pencarian simpul langsung pada array.

### Variabel / Atribut Kelas (Fields)

- `odd` (`Node[]`): Array 1D yang menampung seluruh simpul mata kuliah yang ditawarkan pada semester ganjil.
- `even` (`Node[]`): Array 1D yang menampung seluruh simpul mata kuliah yang ditawarkan pada semester genap.
- `kodes` (`String[]`): Array 1D yang menyimpan seluruh kode unik mata kuliah kurikulum.

### Metode

#### `public CourseCatalog(Node[] odd, Node[] even, String[] kodes)`
- **Fungsi:** Konstruktor untuk menginisialisasi objek katalog dengan array ganjil, array genap, dan daftar kode kurikulum.
- **Parameter:**
  - `odd` (`Node[]`): Array mata kuliah ganjil.
  - `even` (`Node[]`): Array mata kuliah genap.
  - `kodes` (`String[]`): Array kode unik mata kuliah.

#### `public Node[] getOdd()`
- **Fungsi:** Mengambil referensi array mata kuliah semester ganjil.
- **Nilai Kembalian:** `Node[]` berisikan simpul mata kuliah ganjil.

#### `public Node[] getEven()`
- **Fungsi:** Mengambil referensi array mata kuliah semester genap.
- **Nilai Kembalian:** `Node[]` berisikan simpul mata kuliah genap.

#### `public String[] getKodes()`
- **Fungsi:** Mengambil daftar seluruh kode mata kuliah kurikulum. Digunakan pada verifikasi khusus mata kuliah Skripsi (`SKR`) serta penentuan batas kapasitas kelulusan.
- **Nilai Kembalian:** `String[]` seluruh kode mata kuliah.

#### `public Node[] getNodesByKode(String kode)`
- **Fungsi:** Mengambil referensi objek `Node` berdasarkan kode mata kuliah dengan menelusuri langsung array `odd` dan `even`. Mengembalikan 2 node jika mata kuliah berkategori `Keduanya` (`SKR`), 1 node jika reguler, atau 0 node jika tidak ditemukan.
- **Parameter:**
  - `kode` (`String`): Kode mata kuliah yang dicari.
- **Nilai Kembalian:** `Node[]` yang berisi referensi simpul mata kuliah terkait.

---

## 5. Class: CsvLoader

Bertanggung jawab membaca dan mem-parsing berkas CSV (`matakuliah.csv` dan `prasyarat.csv`) serta menghubungkan relasi rantai prasyarat (*Branched SLL*). Menggunakan teknik **Two-Pass Scanning** agar seluruh array berukuran pas (*exact sizing*) tanpa koleksi dinamis.

### Metode

#### `public static CourseCatalog load(String matakuliahPath, String prasyaratPath)`
- **Fungsi:** Mengoordinasikan pembacaan berkas kurikulum dan prasyarat:
  - **Pass 1:** Memindai `matakuliah.csv` untuk menghitung `totalCourses`, `oddCount`, dan `evenCount`.
  - **Alokasi:** Mengalokasikan array `odd`, `even`, dan `kodes` secara eksak.
  - **Pass 2:** Membaca ulang `matakuliah.csv` untuk membuat instansiasi objek `Node` dan mengisinya ke array.
  - **Pass 3:** Membaca `prasyarat.csv` satu putaran untuk menghubungkan pointer `mkNode.prev = prasyaratNode`.
- **Parameter:**
  - `matakuliahPath` (`String`): Lokasi berkas CSV katalog mata kuliah.
  - `prasyaratPath` (`String`): Lokasi berkas CSV relasi prasyarat.
- **Nilai Kembalian:** Objek `CourseCatalog` yang telah terhubung lengkap.
- **Variabel Lokal:**
  - `br1`, `br2`, `brPrasyarat` (`BufferedReader`): Pembaca aliran teks berkas CSV.
  - `totalCourses` (`int`): Jumlah baris mata kuliah unik pada CSV.
  - `oddCount` (`int`): Jumlah mata kuliah kategori ganjil dan keduanya.
  - `evenCount` (`int`): Jumlah mata kuliah kategori genap dan keduanya.
  - `line` (`String`): String penampung baris yang sedang dibaca.
  - `parts` (`String[]`): Array hasil pemotongan string baris berdasarkan delimiter titik koma (`;`).
  - `oddIdx`, `evenIdx`, `kodeIdx` (`int`): Penunjuk indeks pengisian elemen pada array `odd`, `even`, dan `kodes`.
  - `kode`, `nama`, `sks`, `semester`, `tipe` (`String`, `int`): Variabel sementara hasil ekstraksi atribut mata kuliah.
  - `prasyaratKode`, `matakuliahKode` (`String`): Variabel sementara penampung kode prasyarat dan kode mata kuliah tujuan.
  - `prasyaratNodes`, `matakuliahNodes` (`Node[]`): Array referensi node hasil resolusi kode.
  - `prasyaratNode` (`Node`): Referensi simpul prasyarat utama yang dijadikan target pointer `.prev`.

---

## 6. Class: StudentData

Struktur data penampung profil akademik dan rekam jejak kelulusan mahasiswa.

### Variabel / Atribut Kelas (Fields)

- `semesterBerikutnya` (`String`): Target semester yang akan ditempuh mahasiswa (`"ganjil"` atau `"genap"`).
- `lulus` (`String[]`): Array 1D berisikan kode mata kuliah yang telah diselesaikan secara eksplisit oleh mahasiswa.

### Metode

#### `public StudentData(String semesterBerikutnya, String[] lulus)`
- **Fungsi:** Konstruktor untuk membuat objek data mahasiswa.
- **Parameter:**
  - `semesterBerikutnya` (`String`): Target paritas semester.
  - `lulus` (`String[]`): Daftar kode mata kuliah yang lulus.

---

## 7. Class: StudentFile

Kelas pembaca dan parser berkas data profil mahasiswa (`mhs_01.txt`).

### Metode

#### `public static StudentData load(String path)`
- **Fungsi:** Membaca berkas teks mahasiswa, mengekstrak paritas semester berikutnya pada baris pertama dan daftar mata kuliah yang lulus pada baris kedua, lalu membungkusnya ke dalam objek `StudentData`.
- **Parameter:**
  - `path` (`String`): Lokasi berkas data mahasiswa.
- **Nilai Kembalian:** Objek `StudentData` hasil parsing.
- **Variabel Lokal:**
  - `br` (`BufferedReader`): Pembaca aliran teks berkas.
  - `line1` (`String`): String penampung baris pertama (`semester_berikutnya=...`).
  - `semesterBerikutnya` (`String`): Nilai paritas semester hasil pemisahan string.
  - `lulus` (`String[]`): Array hasil pemisahan daftar mata kuliah lulus berdasarkan koma.
  - `line2` (`String`): String penampung baris kedua (`lulus=...`).
  - `parts` (`String[]`): Array hasil pemotongan baris kedua berdasarkan tanda sama dengan (`=`).

---

## 8. Class: EnrollmentChecker

Komponen logika utama yang mengevaluasi kelayakan pengambilan mata kuliah menggunakan penelusuran rantai prasyarat mundur (*Backward SLL Traversal*), penentuan alasan ketidaktersediaan, dan pemformatan tampilan luaran.

### Metode

#### `private static boolean contains(String[] arr, int length, String target)`
- **Fungsi:** Melakukan pencarian linear (*linear search*) untuk memeriksa apakah suatu string `target` berada di dalam array `arr` pada rentang indeks `0` hingga `length - 1`. Menggantikan kebutuhan method `Set.contains()`.
- **Parameter:**
  - `arr` (`String[]`): Array yang diperiksa.
  - `length` (`int`): Jumlah elemen terisi pada array.
  - `target` (`String`): Nilai string yang dicari.
- **Nilai Kembalian:** `true` jika string ditemukan, `false` jika tidak.

#### `private static boolean isOfferedInSemester(CourseCatalog catalog, String kode, String semesterBerikutnya)`
- **Fungsi:** Memeriksa apakah mata kuliah dengan `kode` tertentu ditawarkan pada semester berikutnya. Memeriksa array `odd` jika target semester ganjil, atau array `even` jika target semester genap.
- **Parameter:**
  - `catalog` (`CourseCatalog`): Katalog kurikulum.
  - `kode` (`String`): Kode mata kuliah yang dicek.
  - `semesterBerikutnya` (`String`): Target paritas semester (`"ganjil"` atau `"genap"`).
- **Nilai Kembalian:** `true` jika mata kuliah tersedia di semester tersebut, `false` jika tidak.
- **Variabel Lokal:**
  - `targetArray` (`Node[]`): Array semester target (`odd` atau `even`).

#### `private static String getUnavailableReason(Node node, CourseCatalog catalog, StudentData student, String[] effectiveLulus, int effectiveCount)`
- **Fungsi:** Mengidentifikasi alasan mengapa suatu simpul mata kuliah tidak dapat diambil pada semester berikutnya:
  1. Jika mata kuliah adalah Skripsi (`SKR`) dan ada mata kuliah lain di kurikulum yang belum diselesaikan $\rightarrow$ mengembalikan `"Belum menyelesaikan semua matkul"`.
  2. Jika mata kuliah tidak ditawarkan pada paritas semester berikutnya $\rightarrow$ mengembalikan `"Tidak tersedia pada next semester"`.
  3. Jika mata kuliah memiliki prasyarat (`node.prev != null`) dan prasyaratnya belum tercatat di `effectiveLulus` $\rightarrow$ mengembalikan `"Belum lulus prerequisite"`.
  4. Jika memenuhi seluruh syarat $\rightarrow$ mengembalikan `null` (mata kuliah tersedia).
- **Parameter:**
  - `node` (`Node`): Simpul mata kuliah yang dievaluasi.
  - `catalog` (`CourseCatalog`): Objek katalog kurikulum.
  - `student` (`StudentData`): Profil mahasiswa.
  - `effectiveLulus` (`String[]`): Array mata kuliah yang berstatus lulus (eksplisit + rantai SLL mundur).
  - `effectiveCount` (`int`): Jumlah elemen terisi pada `effectiveLulus`.
- **Nilai Kembalian:** `String` deskripsi alasan penolakan, atau `null` jika mata kuliah dapat diambil.
- **Variabel Lokal:**
  - `allKodes` (`String[]`): Daftar seluruh kode mata kuliah kurikulum untuk validasi kelulusan menyeluruh Skripsi.

#### `private static boolean isAvailable(Node node, CourseCatalog catalog, StudentData student, String[] effectiveLulus, int effectiveCount)`
- **Fungsi:** Menentukan apakah simpul mata kuliah dapat diambil. Mengembalikan `false` jika mata kuliah sudah lulus (ada di `effectiveLulus`) atau jika `getUnavailableReason` mengembalikan non-null.
- **Parameter:**
  - `node` (`Node`): Simpul mata kuliah yang dicek.
  - `catalog` (`CourseCatalog`): Katalog kurikulum.
  - `student` (`StudentData`): Profil mahasiswa.
  - `effectiveLulus` (`String[]`): Array mata kuliah lulus.
  - `effectiveCount` (`int`): Jumlah elemen terisi pada `effectiveLulus`.
- **Nilai Kembalian:** `true` jika mata kuliah dapat diambil, `false` jika tidak.

#### `public static Node[] getAvailableCourses(CourseCatalog catalog, StudentData student)`
- **Fungsi:** Mengumpulkan seluruh mata kuliah yang dapat diambil mahasiswa pada semester berikutnya:
  1. Membentuk array `effectiveLulus` dari daftar kelulusan eksplisit mahasiswa.
  2. Menelusuri seluruh rantai prasyarat mundur (`node.prev` mundur hingga `null`) untuk mengeliminasi mata kuliah yang sudah terpenuhi.
  3. Memilih array semester target (`odd` atau `even`).
  4. Menghitung jumlah mata kuliah yang memenuhi `isAvailable()`.
  5. Mengalokasikan array `Node[] available` berukuran eksak dan mengisinya.
- **Parameter:**
  - `catalog` (`CourseCatalog`): Objek katalog kurikulum.
  - `student` (`StudentData`): Profil mahasiswa.
- **Nilai Kembalian:** `Node[]` berukuran eksak berisikan mata kuliah yang dapat diambil.
- **Variabel Lokal:**
  - `maxCourses` (`int`): Batas kapasitas array `effectiveLulus`.
  - `effectiveLulus` (`String[]`): Array penampung seluruh kode mata kuliah yang telah diselesaikan.
  - `effectiveCount` (`int`): Penghitung jumlah elemen pada `effectiveLulus`.
  - `k` (`String`): Kode mata kuliah sementara dari file mahasiswa.
  - `nodes` (`Node[]`): Array referensi simpul mata kuliah lulus untuk penelusuran prasyarat.
  - `curr` (`Node`): Pointer penelusuran mundur mengikuti rantai `.prev` hingga `null`.
  - `semesterArray` (`Node[]`): Referensi array semester target (`odd` atau `even`).
  - `availableCount` (`int`): Jumlah mata kuliah yang dapat diambil.
  - `available` (`Node[]`): Array eksak penampung akhir mata kuliah yang dapat diambil.
  - `idx` (`int`): Penunjuk indeks pengisian array `available`.

#### `public static UnavailableCourse[] getUnavailableCourses(CourseCatalog catalog, StudentData student)`
- **Fungsi:** Mengidentifikasi seluruh mata kuliah kurikulum yang belum diselesaikan namun tidak dapat diambil pada semester berikutnya beserta alasannya:
  1. Membentuk array `effectiveLulus` secara identik (termasuk rantai SLL mundur).
  2. Menelusuri setiap mata kuliah di kurikulum yang belum berstatus lulus (`!contains(effectiveLulus, ...)`).
  3. Memeriksa alasan penolakan via `getUnavailableReason()`.
  4. Menghitung jumlah mata kuliah yang ditolak, mengalokasikan array `UnavailableCourse[]` berukuran eksak, dan mengisinya.
- **Parameter:**
  - `catalog` (`CourseCatalog`): Objek katalog kurikulum.
  - `student` (`StudentData`): Profil mahasiswa.
- **Nilai Kembalian:** `UnavailableCourse[]` berukuran eksak berisikan mata kuliah yang tidak dapat diambil beserta alasannya.
- **Variabel Lokal:**
  - `maxCourses` (`int`): Batas kapasitas array `effectiveLulus`.
  - `effectiveLulus` (`String[]`): Array penampung kode mata kuliah yang telah diselesaikan.
  - `effectiveCount` (`int`): Penghitung jumlah elemen pada `effectiveLulus`.
  - `allKodes` (`String[]`): Array seluruh kode mata kuliah kurikulum.
  - `count` (`int`): Jumlah mata kuliah yang tidak dapat diambil.
  - `kode` (`String`): Kode mata kuliah yang sedang dievaluasi.
  - `node` (`Node`): Referensi simpul mata kuliah terkait.
  - `reason` (`String`): Alasan penolakan pengambilan mata kuliah.
  - `result` (`UnavailableCourse[]`): Array eksak penampung hasil evaluasi.
  - `idx` (`int`): Penunjuk indeks pengisian array `result`.

#### `public static void printAvailableCourses(StudentData student, Node[] available)`
- **Fungsi:** Mencetak daftar bernomor mata kuliah yang dapat diambil mahasiswa beserta akumulasi total SKS.
- **Parameter:**
  - `student` (`StudentData`): Profil mahasiswa.
  - `available` (`Node[]`): Daftar mata kuliah yang dapat diambil.
- **Variabel Lokal:**
  - `totalSks` (`int`): Akumulator total bobot SKS.
  - `n` (`Node`): Referensi simpul mata kuliah pada perulangan pencetakan.

#### `public static void printUnavailableCourses(UnavailableCourse[] unavailable)`
- **Fungsi:** Mencetak daftar bernomor mata kuliah yang tidak dapat diambil mahasiswa beserta alasan spesifiknya.
- **Parameter:**
  - `unavailable` (`UnavailableCourse[]`): Daftar mata kuliah yang tidak dapat diambil beserta alasannya.
- **Variabel Lokal:**
  - `u` (`UnavailableCourse`): Referensi objek pasangan mata kuliah dan alasan pada perulangan pencetakan.
