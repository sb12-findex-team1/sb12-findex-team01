DROP TABLE IF EXISTS auto_sync;
DROP TABLE IF EXISTS sync_jobs;
DROP TABLE IF EXISTS index_data;
DROP TABLE IF EXISTS index_infos;

CREATE TABLE index_infos
(
	id                   uuid PRIMARY KEY,
	index_name           varchar(100)             NOT NULL,
	index_classification varchar(50)              NOT NULL,
	source_type          varchar(20)              NOT NULL,
	employed_items_count integer                  NULL,
	base_index           numeric(10, 2)           NULL,
	base_point_in_time   date                     NULL,
	favorite             boolean                  NOT NULL,
	created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at           TIMESTAMP WITH TIME ZONE NULL,

	CONSTRAINT uk_index_infos_index_classification_index_name
		UNIQUE (index_classification, index_name)
);

CREATE TABLE index_data
(
	id                  uuid PRIMARY KEY,
	index_info_id       uuid                     NOT NULL,
	base_date           date                     NOT NULL,
	source_type         varchar(20)              NOT NULL,
	market_price        numeric(10, 2)           NULL,
	closing_price       numeric(10, 2)           NULL,
	high_price          numeric(10, 2)           NULL,
	low_price           numeric(10, 2)           NULL,
	versus              numeric(10, 2)           NULL,
	fluctuation_rate    numeric(5, 2)            NULL,
	trading_quantity    bigint                   NULL,
	trading_price       bigint                   NULL,
	market_total_amount bigint                   NULL,
	created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at          TIMESTAMP WITH TIME ZONE NULL,

	CONSTRAINT fk_index_data_index_info FOREIGN KEY (index_info_id)
		REFERENCES index_infos (id) ON DELETE CASCADE,

	CONSTRAINT uk_index_data_index_info_base_date
		UNIQUE (index_info_id, base_date)
);

CREATE TABLE sync_jobs
(
	id            uuid PRIMARY KEY,
	index_info_id uuid                     NOT NULL,
	job_type      varchar(50)              NOT NULL,
	target_date   date                     NULL,
	worker        varchar(100)             NOT NULL,
	job_time      TIMESTAMP WITH TIME ZONE NOT NULL,
	result        varchar(50)              NOT NULL,
	created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at    TIMESTAMP WITH TIME ZONE NULL,

	CONSTRAINT fk_sync_jobs_index_info FOREIGN KEY (index_info_id)
		REFERENCES index_infos (id) ON DELETE CASCADE
);

CREATE TABLE auto_sync
(
	id            uuid PRIMARY KEY,
	index_info_id uuid                     NOT NULL UNIQUE,
	enabled       boolean                  NOT NULL,
	created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at    TIMESTAMP WITH TIME ZONE NULL,

	CONSTRAINT fk_auto_sync_configs_index_info FOREIGN KEY (index_info_id)
		REFERENCES index_infos (id) ON DELETE CASCADE
);
