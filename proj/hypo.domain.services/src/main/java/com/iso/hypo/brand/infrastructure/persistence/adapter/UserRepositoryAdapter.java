package com.iso.hypo.brand.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.brand.application.repository.UserQueryRepository;
import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.brand.domain.repository.UserRepository;
import com.iso.hypo.brand.infrastructure.persistence.entity.UserDocument;
import com.iso.hypo.brand.infrastructure.persistence.mapper.UserDocumentMapper;
import com.iso.hypo.brand.infrastructure.persistence.repository.UserMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

@Repository
public class UserRepositoryAdapter extends BaseAdapter implements UserRepository, UserQueryRepository {

    private final UserMongoRepository mongoRepository;
    private final UserDocumentMapper userMapper;

    public UserRepositoryAdapter(UserMongoRepository mongoRepository, UserDocumentMapper userMapper) {
        this.mongoRepository = mongoRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByEmailAndDeletedIsFalse(String email) {
        return mongoRepository.findByEmailAndDeletedIsFalse(email)
                .map(userMapper::toEntity);
    }

    @Override
    public Optional<User> findByIdpIdAndDeletedIsFalse(String idpId) {
        return mongoRepository.findByIdpIdAndDeletedIsFalse(idpId)
                .map(userMapper::toEntity);
    }

    @Override
    public Optional<User> findByUuidAndDeletedIsFalse(String userUuid) {
        return mongoRepository.findByUuidAndDeletedIsFalse(userUuid)
                .map(userMapper::toEntity);
    }

    @Override
    public PageResult<User> findAllByDeletedIsFalse(PageRequest pageRequest) {
        Page<UserDocument> page = mongoRepository.findAllByDeletedIsFalse(toSpringPageable(pageRequest, Sort.by("lastname").ascending()));
        return toPageResult(page, pageRequest).map(userMapper::toEntity);
    }

    @Override
    public PageResult<User> findAllByDeletedIsFalseAndActiveIsTrue(PageRequest pageRequest) {
        Page<UserDocument> page = mongoRepository.findAllByDeletedIsFalseAndActiveIsTrue(toSpringPageable(pageRequest, Sort.by("lastname").ascending()));
        return toPageResult(page, pageRequest).map(userMapper::toEntity);
    }

    @Override
    public User save(User user) {
        UserDocument document = userMapper.toDocument(user);
        UserDocument saved = mongoRepository.save(document);
        return userMapper.toEntity(saved);
    }

    @Override
    public void delete(User user) {
        mongoRepository.delete(userMapper.toDocument(user));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    // --- Custom methods ---

    @Override
    public PageResult<UserSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest, boolean includeInactive) {
        Page<UserSearchDto> page = mongoRepository.searchAutocomplete(criteria, toSpringPageable(pageRequest, Sort.by("lastname").ascending()), includeInactive);
        return toPageResult(page, pageRequest);
    }
}
