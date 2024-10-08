package com.restapi.usermanagement.adapter.output.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.restapi.usermanagement.adapter.mapper.UserMapper;
import com.restapi.usermanagement.adapter.output.database.entity.DepartmentEntity;
import com.restapi.usermanagement.adapter.output.database.entity.UserEntity;
import com.restapi.usermanagement.adapter.output.database.repository.DepartmentRepository;
import com.restapi.usermanagement.adapter.output.database.repository.UserRepository;
import com.restapi.usermanagement.domain.model.DepartmentModel;
import com.restapi.usermanagement.domain.model.UserModel;
import com.restapi.usermanagement.domain.model.UserRequestModel;

public class UserPersistenceTest {

        @Mock
        private UserMapper userMapper;

        @Mock
        private UserRepository userRepository;

        @Mock
        private DepartmentRepository departmentRepository;

        @InjectMocks
        private UserPersistence userPersistence;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        public void testFindUserInfo_Success() {
                Long userId = 1L;
                UserEntity userEntity = new UserEntity(userId, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", null);
                UserModel userModel = new UserModel(userId, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", null);

                when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
                when(userMapper.toModel(userEntity)).thenReturn(userModel);

                UserModel result = userPersistence.findUserInfo(userId);

                verify(userRepository).findById(userId);
                verify(userMapper).toModel(userEntity);
                assertEquals(userModel, result);
        }

        @Test
        public void testFindUserInfo_UserNotFound() {
                Long userId = 1L;

                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThrows(NoSuchElementException.class, () -> userPersistence.findUserInfo(userId));
        }

        @Test
        public void testCreate_Success() {
                UserRequestModel requestModel = new UserRequestModel("John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", 1L);
                DepartmentEntity departmentEntity = new DepartmentEntity(1L, "HR", null);
                UserEntity userEntity = new UserEntity(null, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", departmentEntity);
                UserModel userModel = new UserModel(1L, "John Doe", "john.doe@example.com", "1234567890", "123 Main St",
                                new DepartmentModel(1L, "HR"));

                when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));
                when(userMapper.toEntity(requestModel)).thenReturn(userEntity);
                when(userRepository.save(userEntity)).thenReturn(userEntity);
                when(userMapper.toModel(userEntity)).thenReturn(userModel);

                UserModel result = userPersistence.create(requestModel);

                verify(departmentRepository).findById(1L);
                verify(userMapper).toEntity(requestModel);
                verify(userRepository).save(userEntity);
                verify(userMapper).toModel(userEntity);
                assertEquals(userModel, result);
        }

        @Test
        public void testCreate_DepartmentNotFound() {
                UserRequestModel requestModel = new UserRequestModel("John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", 1L);

                when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

                assertThrows(NoSuchElementException.class, () -> userPersistence.create(requestModel));
        }

        @Test
        public void testUpdate_Success() {
                Long userId = 1L;
                UserRequestModel requestModel = new UserRequestModel("John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", 1L);
                UserEntity existingUser = new UserEntity(userId, "Jane Doe", "jane.doe@example.com", "0987654321",
                                "456 Another St", null);
                DepartmentEntity departmentEntity = new DepartmentEntity(1L, "HR", null);
                UserEntity updatedUser = new UserEntity(userId, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", departmentEntity);
                UserModel userModel = new UserModel(userId, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", new DepartmentModel(1L, "HR"));

                when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
                when(departmentRepository.findById(1L)).thenReturn(Optional.of(departmentEntity));
                when(userRepository.save(existingUser)).thenReturn(updatedUser);
                when(userMapper.toModel(updatedUser)).thenReturn(userModel);

                UserModel result = userPersistence.update(userId, requestModel);

                verify(userRepository).findById(userId);
                verify(departmentRepository).findById(1L);
                verify(userMapper).updateUserFromModel(requestModel, existingUser);
                verify(userRepository).save(existingUser);
                verify(userMapper).toModel(updatedUser);
                assertEquals(userModel, result);
        }

        @Test
        public void testUpdate_UserNotFound() {
                Long userId = 1L;
                UserRequestModel requestModel = new UserRequestModel("John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", 1L);

                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThrows(NoSuchElementException.class, () -> userPersistence.update(userId, requestModel));
        }

        @Test
        public void testDelete_Success() {
                Long userId = 1L;
                UserEntity existingUser = new UserEntity(userId, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", null);

                when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

                userPersistence.delete(userId);

                verify(userRepository).findById(userId);
                verify(userRepository).deleteById(userId);
        }

        @Test
        public void testDelete_UserNotFound() {
                Long userId = 1L;

                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThrows(NoSuchElementException.class, () -> userPersistence.delete(userId));
        }

        @Test
        public void testFindUserList_Success() {
                Pageable pageable = PageRequest.of(0, 10);
                UserEntity userEntity = new UserEntity(1L, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", null);
                UserModel userModel = new UserModel(1L, "John Doe", "john.doe@example.com", "1234567890", "123 Main St",
                                null);
                Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity), pageable, 1);

                when(userRepository.findList(null, null, null, null, pageable)).thenReturn(userPage);
                when(userMapper.toModel(userEntity)).thenReturn(userModel);

                Page<UserModel> result = userPersistence.findUserList(pageable, null, null, null, null);

                verify(userRepository).findList(null, null, null, null, pageable);
                assertEquals(1, result.getTotalElements());
                assertEquals(userModel, result.getContent().get(0));
        }

        @Test
        public void testFindUserList_NoUsersFound() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<UserEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

                when(userRepository.findList(null, null, null, null, pageable)).thenReturn(emptyPage);

                assertThrows(NoSuchElementException.class,
                                () -> userPersistence.findUserList(pageable, null, null, null, null));
        }

        @Test
        public void testFindUserList_WithFilters() {
                Pageable pageable = PageRequest.of(0, 10);
                UserEntity userEntity = new UserEntity(1L, "John Doe", "john.doe@example.com", "1234567890",
                                "123 Main St", null);
                UserModel userModel = new UserModel(1L, "John Doe", "john.doe@example.com", "1234567890", "123 Main St",
                                null);
                Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity), pageable, 1);

                when(userRepository.findList(1L, "John Doe", "1234567890", 1L, pageable))
                                .thenReturn(userPage);
                when(userMapper.toModel(userEntity)).thenReturn(userModel);

                Page<UserModel> result = userPersistence.findUserList(pageable, 1L, "John Doe",
                                "1234567890", 1L);

                verify(userRepository).findList(1L, "John Doe", "1234567890", 1L, pageable);
                assertEquals(1, result.getTotalElements());
                assertEquals(userModel, result.getContent().get(0));
        }
}
