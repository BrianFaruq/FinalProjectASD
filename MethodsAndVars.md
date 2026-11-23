# Dokumentasi Metode dan Variabel (Methods and Variables Documentation)

**Proyek:** Course Enrollment Availability Checker (Sistem Rekomendasi Rencana Studi)  
**Mata Kuliah:** Algoritma dan Struktur Data (ASD)  
**Penyusun:** Brilliant Faruq Wicaksono  
**NIM:** 255150200111033  

---

## Daftar Isi
1. [Class: Main](#1-class-main)
2. [Class: Node](#2-class-node)
3. [Class: CourseCatalog](#3-class-coursecatalog)
4. [Class: CsvLoader](#4-class-csvloader)
5. [Class: StudentData](#5-class-studentdata)
6. [Class: StudentFile](#6-class-studentfile)
7. [Class: EnrollmentChecker](#7-class-enrollmentchecker)

---

## 1. Class: Main

Kelas utama yang menjadi titik masuk (*entry point*) eksekusi program. Kelas ini bertanggung jawab atas inisialisasi path berkas, pemanggilan parser data, pemanggilan pemeriksa ketersediaan mata kuliah, serta penanganan eksepsi global.

### Metode

#### `public static void main(String[] args)`
- **Fungsi:** Titik masuk eksekusi program Java. Mengatur alur kerja secara berurutan:
  1. Menentukan path berkas data (`matakuliah.csv`, `prasyarat.csv`, `mhs_01.txt`).
  2. Memuat katalog kurikulum dan menghubungkan prasyarat via `CsvLoader.load()`.
  3. Memuat profil mahasiswa via `StudentFile.load()`.
  4. Menyaring mata kuliah yang memenuhi syarat via `EnrollmentChecker.getAvailableCourses()`.
  5. Mencetak daftar mata kuliah dan total SKS via `EnrollmentChecker.printAvailableCourses()`.
- **Parameter:**
  - `args` (`String[]`): Argumen command-line (tidak digunakan karena path diakses secara relatif).
- **Variabel Lokal:**
  - `matakuliahPath` (`String`): Menyimpan path relatif berkas katalog mata kuliah (`"matakuliah.csv"`).
  - `prasyaratPath` (`String`): Menyimpan path relatif berkas graf ketergantungan prasyarat (`"prasyarat.csv"`).
  - `studentPath` (`String`): Menyimpan path relatif berkas profil kelulusan mahasiswa (`"mhs_01.txt"`).
  - `catalog` (`CourseCatalog`): Objek penampung katalog mata kuliah hasil parsing.
  - `student` (`StudentData`): Objek penampung data mahasiswa hasil parsing.
  - `available` (`Node[]`): Array berukuran eksak yang menampung daftar node mata kuliah yang dapat diambil mahasiswa.
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

## 3. Class: CourseCatalog

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

## 4. Class: CsvLoader

Bertanggung jawab membaca dan mem-parsing berkas CSV (`matakuliah.csv` dan `prasyarat.csv`) serta menghubungkan relasi rantai prasyarat (*Branched SLL*). Menggunakan teknik **Two-Pass Scanning** agar seluruh array berukuran pas (*exact sizing*) tanpa koleksi dinamis.

### Metode

#### `public static CourseCatalog load(String matakuliahPath, String prasyaratPath)`
- **Fungsi:** Mengoordinasikan pembacaan berkas kurikulum dan prasyarat:
  - **Pass 1:** Memindai `matakuliah.csv` untuk menghitung `totalCourses`, `oddCount`, dan `evenCount`.
  - **Alokasi:** Mengalokasikan array `odd`, `even`, dan `kodes` secara eksak.
  - **Pass 2:** Membaca ulang `matakuliah.csv` untuk membuat instansiasi objek `Node` dan mengisi array.
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

## 5. Class: StudentData

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

## 6. Class: StudentFile

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

## 7. Class: EnrollmentChecker

Komponen logika utama yang mengevaluasi kelayakan pengambilan mata kuliah menggunakan penelusuran rantai prasyarat mundur (*Backward SLL Traversal*) dan memformat tampilan luaran.

### Metode

#### `private static boolean contains(String[] arr, int length, String target)`
- **Fungsi:** Melakukan pencarian linear (*linear search*) untuk memeriksa apakah suatu string `target` berada di dalam array `arr` pada rentang indeks `0` hingga `length - 1`. Menggantikan kebutuhan method `Set.contains()`.
- **Parameter:**
  - `arr` (`String[]`): Array yang diperiksa.
  - `length` (`int`): Jumlah elemen terisi pada array.
  - `target` (`String`): Nilai string yang dicari.
- **Nilai Kembalian:** `true` jika string ditemukan, `false` jika tidak.

#### `private static boolean isAvailable(Node node, CourseCatalog catalog, String[] effectiveLulus, int effectiveCount)`
- **Fungsi:** Mengevaluasi apakah suatu simpul mata kuliah dapat diambil oleh mahasiswa berdasarkan 3 aturan:
  1. Mata kuliah tidak boleh ada di `effectiveLulus` (tidak boleh ditawarkan jika sudah lulus).
  2. **Aturan Khusus Skripsi (`SKR`):** Skripsi hanya boleh diambil jika seluruh mata kuliah kurikulum selain `SKR` sudah berstatus lulus di `effectiveLulus`.
  3. **Aturan Prasyarat Langsung:** Jika mata kuliah memiliki `node.prev != null`, maka `node.prev.kode` wajib sudah berada di `effectiveLulus`.
- **Parameter:**
  - `node` (`Node`): Simpul mata kuliah yang sedang dievaluasi.
  - `catalog` (`CourseCatalog`): Objek katalog kurikulum.
  - `effectiveLulus` (`String[]`): Array daftar mata kuliah yang berstatus lulus (termasuk rantai prasyarat mundur).
  - `effectiveCount` (`int`): Jumlah elemen aktif di `effectiveLulus`.
- **Nilai Kembalian:** `true` jika mata kuliah memenuhi seluruh syarat ketersediaan, `false` jika tidak.
- **Variabel Lokal:**
  - `allKodes` (`String[]`): Daftar seluruh kode mata kuliah dari katalog untuk evaluasi Skripsi.

#### `public static Node[] getAvailableCourses(CourseCatalog catalog, StudentData student)`
- **Fungsi:** Mengidentifikasi dan mengumpulkan seluruh mata kuliah yang dapat diambil mahasiswa:
  1. Membentuk array `effectiveLulus` dari daftar kelulusan eksplisit mahasiswa.
  2. **Penelusuran Rantai Prasyarat Mundur:** Untuk setiap mata kuliah yang lulus, telusuri pointer `curr = curr.prev` mundur hingga `null`, dan masukkan seluruh simpul pendahulu tersebut ke `effectiveLulus` agar otomatis di-*omit*.
  3. Memilih array semester target (`odd` atau `even`).
  4. **Pass 1:** Menghitung jumlah mata kuliah yang lolos fungsi `isAvailable()`.
  5. **Pass 2:** Mengalokasikan array `Node[] available` dengan ukuran presisi dan mengisinya.
- **Parameter:**
  - `catalog` (`CourseCatalog`): Objek katalog mata kuliah kurikulum.
  - `student` (`StudentData`): Objek profil mahasiswa.
  - **Nilai Kembalian:** `Node[]` berukuran eksak berisikan mata kuliah yang tersedia.
- **Variabel Lokal:**
  - `maxCourses` (`int`): Batas kapasitas maksimum array `effectiveLulus` (diambil dari panjang array `kodes`).
  - `effectiveLulus` (`String[]`): Array penampung seluruh kode mata kuliah yang telah diselesaikan atau dilewati prasyaratnya.
  - `effectiveCount` (`int`): Penghitung jumlah elemen pada `effectiveLulus`.
  - `k` (`String`): Variabel sementara kode mata kuliah lulus dari file mahasiswa.
  - `nodes` (`Node[]`): Array referensi simpul mata kuliah lulus untuk penelusuran prasyarat.
  - `curr` (`Node`): Pointer traversal yang berjalan mundur mengikuti rantai `.prev` hingga `null`.
  - `semesterArray` (`Node[]`): Referensi ke array paritas semester yang dituju (`odd` atau `even`).
  - `availableCount` (`int`): Jumlah mata kuliah yang dinyatakan memenuhi syarat ketersediaan.
  - `available` (`Node[]`): Array eksak penampung akhir mata kuliah yang dapat diambil.
  - `idx` (`int`): Penunjuk indeks pengisian array `available`.

#### `public static void printAvailableCourses(StudentData student, Node[] available)`
- **Fungsi:** Mencetak luaran terformat rapi ke terminal/konsol, meliputi:
  - Header semester yang akan ditempuh.
  - Daftar bernomor mata kuliah (kode, nama, bobot SKS, tingkat semester).
  - Akumulasi total bobot SKS dari seluruh mata kuliah yang ditawarkan.
- **Parameter:**
  - `student` (`StudentData`): Data mahasiswa penampung informasi semester target.
  - `available` (`Node[]`): Array mata kuliah yang tersedia hasil evaluasi.
- **Variabel Lokal:**
  - `totalSks` (`int`): Variabel akumulator penjumlah bobot SKS seluruh mata kuliah yang ditawarkan.
  - `n` (`Node`): Variabel sementara mata kuliah pada perulangan pencetakan.
