CREATE TABLE IF NOT EXISTS public."statistics" (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	player uuid NOT NULL,
	"type" text NOT NULL,
	"name" text NOT NULL,
	value int8 NOT NULL,
	"timestamp" timestamp NOT NULL,
	CONSTRAINT statistics_pk PRIMARY KEY (id),
	CONSTRAINT statistics_un UNIQUE (player, type, name)
);

CREATE TABLE IF NOT EXISTS public.statistics_history (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	stat uuid NOT NULL,
	value int8 NOT NULL,
	"timestamp" timestamp NOT NULL,
	CONSTRAINT statistics_history_pk PRIMARY KEY (id),
	CONSTRAINT statistics_history_fk FOREIGN KEY (stat) REFERENCES public."statistics"(id) ON DELETE CASCADE
);

CREATE OR REPLACE PROCEDURE upsert_statistics(
    IN p_player uuid,
    IN p_type text,
    IN p_name text,
    IN p_value int8
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_stat_id uuid;
    v_prev_value int8;
    v_prev_timestamp timestamp;
BEGIN
    -- Check if a matching row already exists
    SELECT id, value, timestamp
    INTO v_stat_id, v_prev_value, v_prev_timestamp
    FROM statistics
    WHERE player = p_player AND "type" = p_type AND "name" = p_name
    FOR UPDATE;

   	IF v_stat_id IS NOT NULL THEN -- Row already exists, update value and timestamp if necessary
	    IF v_prev_value <> p_value THEN
	        UPDATE statistics
	        SET value = p_value, "timestamp" = NOW()
	        WHERE id = v_stat_id;
	       	-- Insert the new value and timestamp into the history table
		    INSERT INTO statistics_history (stat, value, "timestamp")
		    VALUES (v_stat_id, v_prev_value, v_prev_timestamp);
	    END IF;
	ELSE -- If no row exists, insert a new one
        INSERT INTO statistics (player, "type", "name", value, "timestamp")
        VALUES (p_player, p_type, p_name, p_value, NOW())
        RETURNING id INTO v_stat_id;
    END IF;
END;
$$;