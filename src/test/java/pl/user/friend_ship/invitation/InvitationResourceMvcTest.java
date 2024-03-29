package pl.user.friend_ship.invitation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.user.UserDto;
import pl.user.UserRandomTool;
import pl.user.friend_ship.FriendShipDto;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.tool.JsonTool.asJsonString;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class InvitationResourceMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void inviteReturnedInvitationDtoWhenSecondUserDoNotSendInvitationFirst() throws Exception {
        // given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        InvitationDto expectedInvitationDto = InvitationDto.builder().id(1L)
          .inviter(UserDto.builder().email(userDto.getEmail()).userName(userDto.getUserName()).build())
          .invited(UserDto.builder().email(invitedDto.getEmail()).userName(invitedDto.getUserName()).build())
          .build();
        // when
        // then
        this.mockMvc.perform(post("/api/invite")
            .content(asJsonString(invitedDto.getEmail()))
            .with(user(userDto.getEmail()).password(userDto.getPassword()))
            .contentType(APPLICATION_JSON_VALUE))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(APPLICATION_JSON_VALUE))
          .andExpect(content().json(asJsonString(expectedInvitationDto)));
    }

    @Test
    void inviteReturnedFriendShipDtoWhenSecondUserSendInvitationFirst() throws Exception {
        //given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/invite")
            .content(asJsonString(userDto.getEmail()))
            .with(user(invitedDto.getEmail()).password(invitedDto.getPassword()))
            .contentType(APPLICATION_JSON_VALUE))
          .andDo(print());
        FriendShipDto expectedFriendShipDto = FriendShipDto.builder().id(1L)
          .user(UserDto.builder().email(userDto.getEmail()).build())
          .user2(UserDto.builder().email(invitedDto.getEmail()).build())
          .build();
        // when
        // then
        this.mockMvc.perform(post("/api/invite")
            .content(asJsonString(invitedDto.getEmail()))
            .with(user(userDto.getEmail()).password(userDto.getPassword()))
            .contentType(APPLICATION_JSON_VALUE))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(APPLICATION_JSON_VALUE))
          .andExpect(jsonPath("$.dateOfAdding").isNotEmpty())
          .andExpect(jsonPath("$.user.email").value(expectedFriendShipDto.getUser().getEmail()))
          .andExpect(jsonPath("$.user2.email").value(expectedFriendShipDto.getUser2().getEmail()));
    }

    @Test
    void cancelInvitationNoReturnedAnything() throws Exception {
        //given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/invite")
          .content(asJsonString(userDto.getEmail()))
          .with(user(invitedDto.getEmail()).password(invitedDto.getPassword()))
          .contentType(APPLICATION_JSON_VALUE));
        // when
        // then
        this.mockMvc.perform(delete("/api/invitation/cancel/1")
            .with(user(invitedDto.getEmail()).password(invitedDto.getPassword())))
          .andExpect(status().isNoContent());
    }

    @Test
    void removeInvitationNoReturnedAnything() throws Exception {
        //given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/invite")
          .content(asJsonString(userDto.getEmail()))
          .with(user(invitedDto.getEmail()).password(invitedDto.getPassword()))
          .contentType(APPLICATION_JSON_VALUE));
        // when
        // then
        this.mockMvc.perform(delete("/api/invitation/remove/1")
            .with(user(userDto.getEmail()).password(userDto.getPassword())))
          .andExpect(status().isNoContent());
    }

    @Test
    void getInvitationFromUserReturnedListOfInvitedByUser() throws Exception {
        // given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/invite")
          .content(asJsonString(invitedDto.getEmail()))
          .with(user(userDto.getEmail()).password(userDto.getPassword()))
          .contentType(APPLICATION_JSON_VALUE));
        List<InvitationDto> expectedInvitationDtos = Collections.singletonList(InvitationDto.builder().id(1L)
          .inviter(UserDto.builder().email(userDto.getEmail()).userName(userDto.getUserName()).build())
          .invited(UserDto.builder().email(invitedDto.getEmail()).userName(invitedDto.getUserName()).build())
          .build());
        // when
        // then
        this.mockMvc.perform(get("/api/invitation/from_user")
            .content(asJsonString(invitedDto.getEmail()))
            .with(user(userDto.getEmail()).password(userDto.getPassword()))
            .contentType(APPLICATION_JSON_VALUE))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON_VALUE))
          .andExpect(content().json(asJsonString(expectedInvitationDtos)));

    }

    @Test
    void getInvitationToUserReturnedInvitationSendToInvitedUser() throws Exception {
        // given
        UserDto userDto = UserRandomTool.randomUserDto();
        UserDto invitedDto = UserRandomTool.randomUserDto();
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(userDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/register")
          .content(asJsonString(invitedDto))
          .contentType(APPLICATION_JSON_VALUE));
        this.mockMvc.perform(post("/api/invite")
          .content(asJsonString(invitedDto.getEmail()))
          .with(user(userDto.getEmail()).password(userDto.getPassword()))
          .contentType(APPLICATION_JSON_VALUE));
        List<InvitationDto> expectedInvitationDtos = Collections.singletonList(InvitationDto.builder().id(1L)
          .inviter(UserDto.builder().email(userDto.getEmail()).userName(userDto.getUserName()).build())
          .invited(UserDto.builder().email(invitedDto.getEmail()).userName(invitedDto.getUserName()).build())
          .build());
        // when
        // then
        this.mockMvc.perform(get("/api/invitation/to_user")
            .with(user(invitedDto.getEmail()).password(invitedDto.getPassword()))
            .contentType(APPLICATION_JSON_VALUE))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON_VALUE))
          .andExpect(content().json(asJsonString(expectedInvitationDtos)));

    }

}
