package com.iso.hypo.brand.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.brand.infrastructure.persistence.entity.CourseDocument;
import com.iso.hypo.brand.infrastructure.persistence.mapper.CourseDocumentMapper;
import com.iso.hypo.brand.infrastructure.persistence.repository.CourseMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

@Repository
public class CourseRepositoryAdapter extends BaseAdapter implements CourseRepository {

    private final CourseMongoRepository mongoRepository;
    private final CourseDocumentMapper courseMapper;

    public CourseRepositoryAdapter(CourseMongoRepository mongoRepository, CourseDocumentMapper courseMapper) {
        this.mongoRepository = mongoRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public Optional<Course> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String courseUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, courseUuid)
                .map(courseMapper::toEntity);
    }

    @Override
    public Optional<Course> findByBrandUuidAndCodeAndDeletedIsFalse(String brandUuid, String code) {
        return mongoRepository.findByBrandUuidAndCodeAndDeletedIsFalse(brandUuid, code)
                .map(courseMapper::toEntity);
    }

    @Override
    public PageResult<Course> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<CourseDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("code").ascending()));
        return toPageResult(page, pageRequest).map(courseMapper::toEntity);
    }

    @Override
    public PageResult<Course> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<CourseDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest, Sort.by("code").ascending()));
        return toPageResult(page, pageRequest).map(courseMapper::toEntity);
    }

    @Override
    public Course save(Course course) {
        CourseDocument document = courseMapper.toDocument(course);
        CourseDocument saved = mongoRepository.save(document);
        return courseMapper.toEntity(saved);
    }

    @Override
    public void delete(Course course) {
        mongoRepository.delete(courseMapper.toDocument(course));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }
}
