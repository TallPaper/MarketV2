-- Yildiz Market Modernizasyon SQL Guncelleme Scripti

-- 1. Musteri Sadakat Sistemi Icin Musteri Tablosu
CREATE TABLE IF NOT EXISTS musteriler (
    id SERIAL PRIMARY KEY,
    telefon VARCHAR(20) UNIQUE NOT NULL,
    ad_soyad VARCHAR(100) NOT NULL,
    mail VARCHAR(100),
    puan DECIMAL(10,2) DEFAULT 0
);

-- 2. Satis Detay Tablosuna Iade Kontrol Sutunu
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='satis_detay' AND column_name='iade_edildi') THEN
        ALTER TABLE satis_detay ADD COLUMN iade_edildi BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- 3. Urunler Tablosuna Satis Sayisi Sutunu
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='urunler' AND column_name='satis_sayisi') THEN
        ALTER TABLE urunler ADD COLUMN satis_sayisi INTEGER DEFAULT 0;
    END IF;
END $$;

-- 4. Satislar Tablosuna Musteri Mail Alani
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='satislar' AND column_name='musteri_mail') THEN
        ALTER TABLE satislar ADD COLUMN musteri_mail VARCHAR(100);
    END IF;
END $$;

-- 5. Personel Mail Alani
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='personel' AND column_name='mail') THEN
        ALTER TABLE personel ADD COLUMN mail VARCHAR(100);
    END IF;
END $$;

-- 6. Kampanyalar Tablosuna Baslik Alani
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='kampanyalar' AND column_name='kampanya_baslik') THEN
        ALTER TABLE kampanyalar ADD COLUMN kampanya_baslik VARCHAR(150);
    END IF;
END $$;
