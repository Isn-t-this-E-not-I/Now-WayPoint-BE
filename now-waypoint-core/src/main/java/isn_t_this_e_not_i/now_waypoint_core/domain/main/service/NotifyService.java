package isn_t_this_e_not_i.now_waypoint_core.domain.main.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.repository.NotifyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final NotifyRepository notifyRepository;

    @Transactional
    public List<NotifyDTO> getUserNotify(String loginId){
        List<Notify> userNotifyList = notifyRepository.findByReceiverNickname(loginId);
        return getNotifyDTO(userNotifyList);
    }

    @Transactional
    public void deleteNotify(Long id) {
        notifyRepository.deleteById(id);
    }

    private List<NotifyDTO> getNotifyDTO(List<Notify> userNotifyList){
        List<NotifyDTO> NotifyDTOList = new ArrayList<>();
        for (Notify notify : userNotifyList) {
            NotifyDTO notifyDTO = NotifyDTO.builder()
                    .id(notify.getId())
                    .nickname(notify.getSenderNickname())
                    .message(notify.getMessage())
                    .profileImageUrl(notify.getProfileImageUrl())
                    .createDate(notify.getCreateDate())
                    .build();
            NotifyDTOList.add(notifyDTO);
        }
        return NotifyDTOList;
    }
}
