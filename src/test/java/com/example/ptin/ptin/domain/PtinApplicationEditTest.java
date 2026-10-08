package com.example.ptin.ptin.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotEditableException;
import com.example.ptin.ptin.domain.model.*;
import com.example.ptin.shared.identity.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class PtinApplicationEditTest {

    private static PtinApplication pending() {
        return PtinApplication.submit(
                UserId.generate(), PtinType.INDIVIDUAL,
                new PersonalInfo(null, "A", "B", "M", "LA", "19900101", null, null, null),
                new ContactInfo(null, null, null, null), new AddressInfo(null, null, null, null, null),
                new EmploymentInfo(null, null, null, null, null, null, null, null, null, null),
                new IncomeInfo(null, null, null), new FinancialInfo(null, null));
    }

    private static void edit(PtinApplication a, String givenName) {
        a.edit(PtinType.INDIVIDUAL, new PersonalInfo(null, givenName, "B", "M", "LA", "19900101", null, null, null),
                a.getContactInfo(), a.getAddressInfo(), a.getEmploymentInfo(), a.getIncomeInfo(), a.getFinancialInfo());
    }

    @Test
    void editableWhilePendingButNotAfterApproval() {
        var a = pending();
        edit(a, "Z");
        assertEquals("Z", a.getPersonalInfo().givenName());
        a.approve(UserId.generate());
        assertThrows(PtinApplicationNotEditableException.class, () -> edit(a, "Y"));
    }

    @Test
    void superadminInheritsAdmin() {
        var h = RoleHierarchyImpl.fromHierarchy("ROLE_SUPERADMIN > ROLE_ADMIN");
        var reachable = h.getReachableGrantedAuthorities(java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
        assertEquals(true, reachable.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
