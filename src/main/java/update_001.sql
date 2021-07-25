CREATE TABLE company                                                                                                                     
(                                                                                                                                        
    id integer NOT NULL,                                                                                                                 
    name character varying,                                                                                                              
    CONSTRAINT company_pkey PRIMARY KEY (id)                                                                                             
);                                                                                                                                       
                                                                                                                                         
CREATE TABLE person                                                                                                                      
(                                                                                                                                        
    id integer NOT NULL,                                                                                                                 
    name character varying,                                                                                                              
    company_id integer references company(id),                                                                                           
    CONSTRAINT person_pkey PRIMARY KEY (id)                                                                                              
);                                                                                                                                       
                                                                                                                                         
select p.name, c.name from person p join company c on p.company_id = c.id where c.id != 5;                                               
select select c.name, count(p.name) from company c join person p on p.company_id=c.id group by c.name order by count(p.id) desc limit 1; 
