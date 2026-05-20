package com.codeit.findex.entity;

import com.codeit.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sync_jobs")
@Getter
@SuperBuilder
@ToString(callSuper = true, exclude = "indexInfo")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncJob extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, length = 50)
  private JobType jobType;

  @Column(name = "target_date")
  private LocalDate targetDate;

  @Column(name = "worker", nullable = false, length = 100)
  private String worker;

  @Column(name = "job_time", nullable = false)
  private Instant jobTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 50)
  private Result result;

  public static SyncJob success(IndexInfo indexInfo, JobType jobType, LocalDate targetDate, String worker) {
    return SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(jobType)
        .targetDate(targetDate)
        .worker(worker)
        .jobTime(Instant.now())
        .result(Result.SUCCESS)
        .build();
  }

  public static SyncJob failed(IndexInfo indexInfo, JobType jobType, LocalDate targetDate, String worker) {
    return SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(jobType)
        .targetDate(targetDate)
        .worker(worker)
        .jobTime(Instant.now())
        .result(Result.FAILED)
        .build();
  }

}