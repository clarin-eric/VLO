--
-- PostgreSQL database dump
--

-- Dumped from database version 11.4 (Ubuntu 11.4-1.pgdg18.04+1)
-- Dumped by pg_dump version 11.4 (Ubuntu 11.4-1.pgdg18.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: PageViews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PageViews" (
    id integer NOT NULL,
    record_id character varying,
    ip character varying,
    url character varying,
    http_referer character varying,
    "timeSt" timestamp without time zone
);


--
-- Name: PageViews_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."PageViews_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: PageViews_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."PageViews_id_seq" OWNED BY public."PageViews".id;


--
-- Name: SearchQueries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."SearchQueries" (
    id integer NOT NULL,
    "searchTerm" character varying,
    filter character varying,
    url character varying,
    ip character varying,
    "timeSt" timestamp without time zone
);


--
-- Name: SearchResults; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."SearchResults" (
    id integer NOT NULL,
    query_id integer,
    record_id character varying,
    "position" integer,
    page integer
);


--
-- Name: SearchResults_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."SearchResults_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: SearchResults_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."SearchResults_id_seq" OWNED BY public."SearchQueries".id;


--
-- Name: SearchResults_id_seq1; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."SearchResults_id_seq1"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: SearchResults_id_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."SearchResults_id_seq1" OWNED BY public."SearchResults".id;


--
-- Name: PageViews id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PageViews" ALTER COLUMN id SET DEFAULT nextval('public."PageViews_id_seq"'::regclass);


--
-- Name: SearchQueries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SearchQueries" ALTER COLUMN id SET DEFAULT nextval('public."SearchResults_id_seq"'::regclass);


--
-- Name: SearchResults id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SearchResults" ALTER COLUMN id SET DEFAULT nextval('public."SearchResults_id_seq1"'::regclass);


--
-- Name: PageViews PageViews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PageViews"
    ADD CONSTRAINT "PageViews_pkey" PRIMARY KEY (id);


--
-- Name: SearchQueries SearchResults_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SearchQueries"
    ADD CONSTRAINT "SearchResults_pkey" PRIMARY KEY (id);


--
-- Name: SearchResults SearchResults_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SearchResults"
    ADD CONSTRAINT "SearchResults_pkey1" PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

