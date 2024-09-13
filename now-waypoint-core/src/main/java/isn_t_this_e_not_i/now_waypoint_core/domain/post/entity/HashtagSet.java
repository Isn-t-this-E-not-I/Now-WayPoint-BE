package isn_t_this_e_not_i.now_waypoint_core.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HashtagSet {

    @Id @GeneratedValue
    @Column(name = "hashtagSet_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
