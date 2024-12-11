package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "menu_image")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Menuimage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no")
    private Long no;

    @Column(name = "filename", length = 300)
    private String filename;

    @Column(name = "filetype", length = 50)
    private String filetype;

    @Column(name = "filesize")
    private Long filesize;

    /// 이미지 데이터 저장을 위한 필드
    @Lob
    @Column(name = "filedata",columnDefinition = "MEDIUMBLOB" )
    private byte[] filedata;

    // 외래 키로 Menu와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imageno", referencedColumnName = "menu_id")
    private Menu menu;  // 메뉴 엔티티와의 관계 설정

}
