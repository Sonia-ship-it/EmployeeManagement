-- PostgreSQL helper function (Java MessageService is the primary message generator)

CREATE OR REPLACE FUNCTION fn_build_payroll_message(
    p_first_name VARCHAR,
    p_month INT,
    p_year INT,
    p_institution VARCHAR,
    p_amount NUMERIC,
    p_employee_code VARCHAR
) RETURNS TEXT AS $$
DECLARE
    v_month_year TEXT;
BEGIN
    v_month_year := TO_CHAR(TO_DATE(p_month::TEXT || '-' || p_year::TEXT, 'MM-YYYY'), 'Month YYYY');
    RETURN 'Dear ' || p_first_name ||
           ' Your salary of ' || TRIM(v_month_year) ||
           ' from ' || COALESCE(p_institution, 'Government') ||
           ' ' || p_amount ||
           ' has been credited to your ' || p_employee_code ||
           ' account Successfully';
END;
$$ LANGUAGE plpgsql;;
