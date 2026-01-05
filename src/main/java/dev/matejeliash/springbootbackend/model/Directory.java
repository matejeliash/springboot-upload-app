package dev.matejeliash.springbootbackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@Setter
@Table(name="directories")
public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Directory parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Column(nullable = false)
    private LocalDateTime createdAt;

    public String getFullPath() {
        if (parent == null) return name;
        return parent.getFullPath() + "/" + name;
    }

    @Override
    public String toString() {
        return "Directory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + (parent != null ? parent.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }


    public Directory(){}

}
