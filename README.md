EN4S
====

Yuklemek icin yapmaniz gerekenler

1)libs klasoru icindeki libs.zip in icindekileri cikarin
2)Android SDK Manageri calistirip, Extras kategorisinden Google Play services i yukleyin
3)Simdi 3 adet kutuphaneyi Eclipse import edecegiz.
   .libs.zip icinden cikan 2 kutuphaneyi, import edin
   .<sdk klasorunuz>\extras\google\google_play_services i import edin.
   
4)TumSiniflar projesini (github dan cekeceginiz proje) ye sag tiklayip Proporties e girin, soldaki menuden androidi secin
Library kismina 3 adet kutuphane ekleyecegiz.Add ile tek tek sunlari ekleyin
  .LauncherActivityLibrary
  .PagerSliderLibrary
  .google_play_services

5) ve son olarak AndroidManifest icinde bulunan
        <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="AIzaSyBI-Vyyh8nT4FH60hTRhKmuUkUkIf48l5A" />
            
    kismindaki API KEY i kendinize ozel olan API key ile degistirin. API-KEY almayi bilmiyorsaniz kadiranilturgut.com
    da anlatmistim :D:D 
