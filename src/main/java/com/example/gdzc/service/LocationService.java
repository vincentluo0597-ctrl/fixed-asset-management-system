package com.example.gdzc.service;

import com.example.gdzc.domain.Location;
import com.example.gdzc.dto.LocationDTO;
import com.example.gdzc.dto.LocationTreeDTO;
import com.example.gdzc.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final OperationLogService operationLogService;

    public List<Location> list() {
        return locationRepository.findAll();
    }

    @Transactional
    public Location create(LocationDTO dto) {
        String name = dto.getName() != null ? dto.getName().trim() : null;
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("位置名称不能为空");
        }
        locationRepository.findByName(name).ifPresent(l -> {
            throw new IllegalArgumentException("位置名称已存在");
        });
        Integer level = dto.getLevel();
        if (level == null) {
            level = (dto.getParentId() == null) ? 1 : 2; // 简单默认：无父为1级，有父为2级
        }
        Location location = Location.builder()
                .name(name)
                .type(dto.getType())
                .managerId(dto.getManagerId())
                .parentId(dto.getParentId())
                .level(level)
                .build();
        Location saved = locationRepository.save(location);
        operationLogService.logWithCurrentUser("Location", saved.getId(), "CREATE", "创建位置: " + saved.getName());
        return saved;
    }

    public List<Location> children(Long parentId) {
        return locationRepository.findByParentId(parentId);
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Transactional
    public Location update(Long id, LocationDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("位置不存在"));
        String name = dto.getName() != null ? dto.getName().trim() : null;
        if (StringUtils.hasText(name)) {
            location.setName(name);
        }
        location.setType(dto.getType());
        location.setManagerId(dto.getManagerId());
        location.setParentId(dto.getParentId());
        location.setLevel(dto.getLevel());
        Location saved = locationRepository.save(location);
        operationLogService.logWithCurrentUser("Location", saved.getId(), "UPDATE", "更新位置: " + saved.getName());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        if (locationRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("该位置存在子节点，无法删除");
        }
        locationRepository.deleteById(id);
        operationLogService.logWithCurrentUser("Location", id, "DELETE", "删除位置");
    }

    public List<LocationTreeDTO> buildTree() {
        List<Location> all = locationRepository.findAll();
        Map<Long, LocationTreeDTO> dtoMap = new HashMap<>();
        for (Location l : all) {
            LocationTreeDTO dto = LocationTreeDTO.builder()
                    .id(l.getId())
                    .name(l.getName())
                    .type(l.getType())
                    .managerId(l.getManagerId())
                    .parentId(l.getParentId())
                    .level(l.getLevel())
                    .children(new ArrayList<>())
                    .build();
            dtoMap.put(l.getId(), dto);
        }
        List<LocationTreeDTO> roots = new ArrayList<>();
        for (Location l : all) {
            if (l.getParentId() == null) {
                roots.add(dtoMap.get(l.getId()));
            } else {
                LocationTreeDTO parent = dtoMap.get(l.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dtoMap.get(l.getId()));
                } else {
                    // 如果找不到父节点（数据异常），作为根处理
                    roots.add(dtoMap.get(l.getId()));
                }
            }
        }
        // 按 level 和 name 排序每个层级的 children，提升可读性
        sortChildrenRecursively(roots);
        return roots;
    }

    private void sortChildrenRecursively(List<LocationTreeDTO> nodes) {
        if (nodes == null) return;
        nodes.sort(Comparator.comparing((LocationTreeDTO n) -> Optional.ofNullable(n.getLevel()).orElse(0))
                .thenComparing(n -> Optional.ofNullable(n.getName()).orElse("")));
        for (LocationTreeDTO n : nodes) {
            sortChildrenRecursively(n.getChildren());
        }
    }
}