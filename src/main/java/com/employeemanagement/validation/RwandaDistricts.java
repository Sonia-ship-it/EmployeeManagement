package com.employeemanagement.validation;

import java.util.Set;

/**
 * Official 30 districts of Rwanda — used to validate the {@code district} field on employees.
 *
 * <p>Referenced by {@link ValidDistrict} annotation on {@link com.employeemanagement.dto.EmployeeRequest}.</p>
 */
public final class RwandaDistricts {

    public static final Set<String> DISTRICTS = Set.of(
            "Burera", "Gakenke", "Gicumbi", "Musanze", "Rulindo",
            "Gisagara", "Huye", "Kamonyi", "Muhanga", "Nyamagabe", "Nyanza", "Nyaruguru", "Ruhango",
            "Karongi", "Ngororero", "Nyabihu", "Nyamasheke", "Rubavu", "Rusizi", "Rutsiro",
            "Bugesera", "Gatsibo", "Kayonza", "Kirehe", "Ngoma", "Nyagatare", "Rwamagana",
            "Gasabo", "Kicukiro", "Nyarugenge"
    );

    private RwandaDistricts() {
    }

    public static boolean isValid(String district) {
        return district != null && DISTRICTS.contains(district);
    }
}
