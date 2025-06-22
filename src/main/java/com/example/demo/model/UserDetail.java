package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user_details")
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String fullName;

    private String contactNumber;
    private String email;
    private String address;

    @OneToOne
    @JoinColumn(name = "user_credential_id", nullable = false) // foreign key column
    private UserCredential userCredential; // This field resolves your error

    @OneToMany(mappedBy = "userDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHistory> history = new ArrayList<>();

    public UserDetail() {}

    public UserDetail(String username, String fullName, String contactNumber, String email, String address) {
        this.username = username;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }

    public void addHistory(UserHistory entry) {
        history.add(entry);
        entry.setUserDetail(this);
    }

    public void removeHistory(UserHistory entry) {
        history.remove(entry);
        entry.setUserDetail(null);
    }
}
