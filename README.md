# PalapaNet Portal (APK)

Aplikasi Android sederhana yang membuka portal pelanggan https://palapanetstb.duckdns.org:121/c
Semua isi (tagihan, pembayaran) tetap dari server; aplikasi hanya pembungkus (WebView). Tidak ada library tambahan.

## Cara membuat APK di GitHub
1. Buat repository baru di github.com (boleh Private).
2. Unggah SELURUH isi folder ini ke repository (termasuk folder `.github`, `app`, dan file di root).
   - Lewat web: Add file > Upload files, seret semua isi folder. Pastikan `.github/workflows/build-apk.yml` ikut.
   - Atau lewat git: `git init && git add . && git commit -m "apk" && git branch -M main && git remote add origin <url-repo> && git push -u origin main`
3. Buka tab **Actions**. Build jalan otomatis setelah unggah (atau tekan "Run workflow"). Tunggu sekitar 3-6 menit sampai hijau.
4. Buka build yang selesai, bagian **Artifacts** di bawah, unduh `PalapaNet-portal-apk` (zip), ekstrak, ambil `PalapaNet-portal.apk`.
5. Kirim APK ke HP lalu pasang (izinkan "pasang dari sumber tidak dikenal").

## Ubah alamat / nama
Edit bagian paling atas `app/build.gradle` (PORTAL_URL dan APP_NAME), commit, lalu build ulang.

## Supaya pembaruan APK bisa menimpa versi lama (disarankan)
Tanpa langkah ini, tiap build ditandatangani kunci acak sehingga versi baru harus menghapus versi lama dulu.
1. Di komputer/server berisi Java: 
   `keytool -genkeypair -v -keystore release.jks -alias palapanet -keyalg RSA -keysize 2048 -validity 10000`
   (simpan file `release.jks` dan kata sandinya baik-baik; jangan diunggah ke repo)
2. `base64 -w0 release.jks` lalu salin hasilnya.
3. Di GitHub: Settings > Secrets and variables > Actions > New repository secret, buat 4 secret:
   `KEYSTORE_BASE64` (hasil langkah 2), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (palapanet), `KEY_PASSWORD`.
4. Jalankan ulang workflow. APK berikutnya selalu memakai kunci yang sama.
