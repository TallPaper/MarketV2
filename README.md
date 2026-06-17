# 🛒 Yıldız Market - Ticari Otomasyon ve Kasa (POS) Sistemi

Yıldız Market Ticari Otomasyon Sistemi, marketler için geliştirilmiş, Java Swing tabanlı, modern arayüze ve güçlü bir PostgreSQL veritabanı altyapısına sahip profesyonel bir otomasyon ve Kasa (POS) yazılımıdır. 

Proje; gelişmiş güvenlik özellikleri, hızlı ürün satışı, stok takibi ve otomatik e-posta raporlama gibi modülleriyle uçtan uca bir ticari otomasyon çözümü sunar.

---

## ✨ Özellikler

*   **🔒 Gelişmiş Giriş ve Güvenlik Sistemi:** Rol tabanlı erişim kontrolü (Yönetici / Kasa Görevlisi) ve güvenli giriş paneli.
*   **💻 Modern Kasa (POS) Arayüzü:** Hızlı ürün butonları ("Hızlı Ürünler") ile veritabanından dinamik barkod sorgulama ve anında sepete ekleme.
*   **🛒 Sepette Ürün Birleştirme:** Sepete aynı ürün eklendiğinde ayrı satır oluşturmak yerine mevcut satırdaki miktar ve toplam tutar güncellenir.
*   **📦 Stok ve Envanter Yönetimi:** Kritik stok seviyesi kontrolü, otomatik stok düşümü ve ürün ekleme/güncelleme işlemleri.
*   **🔄 Gelişmiş ve Kısmi İade Sistemi:** İade ekranında ürünler onay kutuları (checkbox) ile çoklu seçilebilir. Ürün miktarı 1'den fazla ise iade edilecek miktar sorulur, kısmi iadeler takip edilir ve kalan miktarlar daha sonra iade edilebilir. İade edilen miktar anında ürün stoğuna geri eklenir. Zaten iade edilmiş ürünler satır bazında grileştirilerek engellenir.
*   **🧾 Termal İade Fişi:** Tamamlanan iade işleminin ardından iade edilen ürünleri, miktarları, birim ve toplam tutarları gösteren termal fiş çıktısı ekrana yansıtılır.
*   **📅 Kampanya Zaman Yönetimi:** Kampanyaların başlangıç ve bitiş tarihleri yönetim panelindeki tabloda listelenir.
*   **🧹 Otomatik Kampanya Temizliği & Bitiş Bildirimi:** Süresi dolan kampanyalar panel açılışında veritabanından otomatik olarak temizlenir ve üyelerin e-posta adreslerine kampanyanın sona erdiğine dair bilgilendirme maili gönderilir.
*   **📊 Veritabanı Düzeyinde İş Mantığı:** PostgreSQL üzerinde çalışan stored function'lar, trigger'lar, otomatik loglama ve audit trail yapıları sayesinde yüksek veri tutarlılığı.
*   **📧 Otomatik E-Posta Servisi:** Günlük satış özetleri ve kritik seviyedeki stok uyarılarını otomatik olarak yöneticiye e-posta yoluyla raporlayan entegre JavaMail entegrasyonu.
*   **🎨 Premium Arayüz:** Kullanıcı dostu, modern sistem temasıyla uyumlu ve dinamik Swing arayüzü.

---

## 🛠️ Kullanılan Teknolojiler

*   **Dil:** Java SE
*   **Arayüz Kütüphanesi:** Java Swing (System Look and Feel)
*   **Veritabanı:** PostgreSQL (Stored Procedures, Triggers, Views)
*   **Harici Kütüphaneler (lib/):**
    *   `postgresql-42.7.3.jar` (PostgreSQL JDBC Sürücüsü)
    *   `javax.mail.jar` & `activation.jar` (E-posta Gönderim Servisi)
    *   `jcalendar-1.4.jar` (Tarih Seçim Bileşeni)
*   **Güvenlik & Yapılandırma:** `.env` (Java Dotenv) ile hassas verilerin saklanması.

---

## 📂 Proje Yapısı

```text
MarketV2/
├── bin/                    # Derlenmiş sınıf dosyaları (Git tarafından yoksayılır)
├── lib/                    # Proje için gerekli .jar kütüphaneleri
├── src/
│   ├── controller/         # İş mantığı ve arayüz kontrolleri
│   ├── db/                 # Veritabanı bağlantı yönetimi
│   ├── model/              # Veri modelleri ve veri tabanı nesneleri
│   ├── service/            # E-posta servisi vb. yardımcı servisler
│   ├── view/               # Swing ekranları ve UI bileşenleri
│   └── Main.java           # Uygulama giriş noktası
├── .env.example            # Çevre değişkenleri şablonu
├── .gitignore              # Git tarafından takip edilmeyecek dosyalar listesi
├── database_setup.txt      # PostgreSQL veritabanı şeması ve başlangıç verileri
├── update_database.sql     # Veritabanı fonksiyon ve trigger güncellemeleri
└── README.md               # Proje tanıtım ve kurulum belgesi
```

---

## 🚀 Kurulum ve Çalıştırma

### 1. Gereksinimler
*   **Java JDK 17** veya üzeri yüklü olmalıdır.
*   **PostgreSQL** veritabanı sunucusu çalışır durumda olmalıdır.

### 2. Projeyi Klonlayın
```bash
git clone <repository-url>
cd MarketV2
```

### 3. Veritabanı Kurulumu
1.  PostgreSQL üzerinde `marketdb` adında bir veritabanı oluşturun.
2.  `database_setup.txt` dosyasındaki SQL komutlarını sırasıyla çalıştırarak tabloları ve temel verileri oluşturun.
3.  `update_database.sql` içerisindeki fonksiyonları, trigger'ları ve ilişkili iş mantığını veritabanınıza uygulayın.

### 4. Yapılandırma (.env Ayarları)
Kök dizinde yer alan `.env.example` dosyasının adını `.env` olarak değiştirin ve içeriğini kendi PostgreSQL ve e-posta bilgilerinizle doldurun:

```env
# Veritabanı Ayarları
DB_URL=jdbc:postgresql://localhost:5432/marketdb
DB_USER=postgres
DB_PASSWORD=veritabanı_şifreniz

# Mail Sunucu Ayarları (Gmail App Password kullanılması önerilir)
MAIL_SENDER=gonderici_mail@gmail.com
MAIL_PASSWORD=gmail_uygulama_sifreniz
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
```

### 5. Kütüphaneleri Dahil Etme
IDE'nizde (VS Code, Eclipse veya IntelliJ IDEA) `lib/` klasörü içerisindeki tüm `.jar` dosyalarını projenizin **ClassPath / Referenced Libraries** kısmına ekleyin.

### 6. Çalıştırın
`src/Main.java` dosyasını çalıştırarak uygulamayı başlatabilirsiniz.

---

## 🔒 Güvenlik Notu
Bu projede kullanılan `.env` dosyası hassas veritabanı şifrelerini ve mail uygulama şifrelerini içerdiğinden kesinlikle Git/GitHub ortamına yüklenmemelidir. Bu durum `.gitignore` dosyasında önceden yapılandırılmıştır.
