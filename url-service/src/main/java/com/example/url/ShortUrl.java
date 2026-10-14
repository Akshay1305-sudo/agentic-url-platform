package com.example.url;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="short_urls", uniqueConstraints=@UniqueConstraint(columnNames="code")) public class ShortUrl {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=2048) public String targetUrl;
 @Column(nullable=false,unique=true,length=16) public String code;
 public Instant createdAt=Instant.now(); public Instant expiresAt;
 public ShortUrl(){} public ShortUrl(String targetUrl,String code,Instant expiresAt){this.targetUrl=targetUrl;this.code=code;this.expiresAt=expiresAt;}
}
