# Sakura Companion (Fabric 1.21.11)

Pendamping animasi: ngobrol, kasih hadiah, pacaran, menikah, punya anak. Ramah semua umur.

## Cara build dari HP (tanpa PC)
1. Buat akun GitHub, buat repository baru (kosong).
2. Upload semua isi folder ini (termasuk folder `.github`) lewat "Add file > Upload files".
3. Buka tab **Actions**, tunggu "Build mod" selesai (centang hijau).
4. Buka run tersebut, unduh artifact **sakura-companion-jar**, ekstrak, ambil file `sakura-companion-1.0.0.jar` (bukan yang `-sources`).
5. Taruh jar di folder mods, bersama **GeckoLib 5.4.4 untuk Fabric 1.21.11** (wajib, unduh dari Modrinth).

Kalau build merah, salin pesan error-nya dan kirim ke Claude untuk diperbaiki.

## Cara main
- Spawn: telur spawn Sakura (tab Spawn Eggs, mode kreatif).
- Klik kanan tangan kosong: ngobrol (+kedekatan, ada jeda 20 detik).
- Hadiah (klik kanan): bunga poppy, pink petals, cherry leaves, sweet berries, cookie, golden carrot (+5), diamond (+10).
- Kedekatan 30: otomatis **pacaran**, Sakura ikut kamu dan membantu bertarung.
- Cincin (resep: 1 diamond + 3 gold ingot) + kedekatan 100: **menikah**.
- Setelah menikah, klik kanan dengan **kue / pumpkin pie**: anak lahir (jeda 10 menit).
- Shift + klik kanan tangan kosong: duduk / berdiri.
- Klik kanan dengan piringan musik: menari.
- Klik kanan dengan **pewarna pink (pink dye)**: cium. Hanya bisa kalau sudah pacaran atau menikah, tambah kedekatan.
- Momen anak lahir ditampilkan non-eksplisit (teks + hati), tanpa adegan dewasa.

## Bangunan
Di tab Tools and Utilities (mode kreatif):
- **Tongkat Rumah Sakit**: klik kanan di tanah, rumah sakit 17x13 terbangun (ruang bersalin 4 tempat tidur, lobi, resepsionis, palang merah).
- **Tongkat Kota**: rumah sakit + 4 rumah warga + jalan dan lampu.
Bangunan membentang ke arah timur dan selatan dari blok yang diklik. Area dikosongkan dulu, jadi pilih tanah lapang (atau di dunia cadangan).
