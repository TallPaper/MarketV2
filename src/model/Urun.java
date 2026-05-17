package model;

public class Urun {
    private int id;
    private String barkod;
    private String urunAdi;
    private String kategori;
    private double stok;
    private String birim;
    private double alisFiyati;
    private double fiyat;
    private String tedarikci;
    private double satisSayisi;

    public Urun(int id, String barkod, String urunAdi, String kategori, double stok, String birim, double alisFiyati, double fiyat, String tedarikci, double satisSayisi) {
        this.id = id;
        this.barkod = barkod;
        this.urunAdi = urunAdi;
        this.kategori = kategori;
        this.stok = stok;
        this.birim = birim;
        this.alisFiyati = alisFiyati;
        this.fiyat = fiyat;
        this.tedarikci = tedarikci;
        this.satisSayisi = satisSayisi;
    }

    // Getters
    public int getId() { return id; }
    public String getBarkod() { return barkod; }
    public String getUrunAdi() { return urunAdi; }
    public String getKategori() { return kategori; }
    public double getStok() { return stok; }
    public String getBirim() { return birim; }
    public double getAlisFiyati() { return alisFiyati; }
    public double getFiyat() { return fiyat; }
    public String getTedarikci() { return tedarikci; }
    public double getSatisSayisi() { return satisSayisi; }
}
