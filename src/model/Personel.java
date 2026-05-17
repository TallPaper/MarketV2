package model;

public class Personel {
    private int id;
    private String kullaniciNo;
    private String ad;
    private String soyad;
    private String rol;
    private String sifre;
    private String mail;

    public Personel(int id, String kullaniciNo, String ad, String soyad, String rol, String sifre, String mail) {
        this.id = id;
        this.kullaniciNo = kullaniciNo;
        this.ad = ad;
        this.soyad = soyad;
        this.rol = rol;
        this.sifre = sifre;
        this.mail = mail;
    }

    public int getId() { return id; }
    public String getKullaniciNo() { return kullaniciNo; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getAdSoyad() { return ad + " " + soyad; }
    public String getRol() { return rol; }
    public String getSifre() { return sifre; }
    public String getMail() { return mail; }
}
