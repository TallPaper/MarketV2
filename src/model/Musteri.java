package model;

public class Musteri {
    private int id;
    private String telefon;
    private String adSoyad;
    private String mail;
    private double puan;

    public Musteri(int id, String telefon, String adSoyad, String mail, double puan) {
        this.id = id;
        this.telefon = telefon;
        this.adSoyad = adSoyad;
        this.mail = mail;
        this.puan = puan;
    }

    public int getId() { return id; }
    public String getTelefon() { return telefon; }
    public String getAdSoyad() { return adSoyad; }
    public String getMail() { return mail; }
    public double getPuan() { return puan; }
    
    public void setPuan(double puan) { this.puan = puan; }
}
