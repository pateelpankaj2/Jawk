package com.mpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "USER_PROFILE")
@JsonIgnoreProperties(ignoreUnknown = true)
@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
@SQLDelete(sql = "UPDATE USER_PROFILE SET is_deleted = true, date_modified = now() WHERE USER_ID = ?") // Soft delete
public class UserProfile extends TrackChangeEntity implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @NotNull
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @NotNull
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "SINGLE_PAYOUT_MIN_AMOUNT")
    private BigDecimal singlePayoutMinAmount;

    @Column(name = "SINGLE_PAYOUT_MAX_AMOUNT")
    private BigDecimal singlePayoutMaxAmount;

    @Column(name = "DAILY_PAYOUT_LIMIT_AMOUNT")
    private BigDecimal dailyPayoutLimitAmount;

    @NotNull
    @Column(name = "USERNAME", nullable = false)
    private String username;

    @NotNull
    @Column(name = "PASSWORD", nullable = false)
    private String password;

	@Column(name = "SETTLEMENT_PASSWORD")
	private String settlementPassword;

    @Column(name = "EMAIL")
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID")
    private Roles role;

    @Column(name = "IS_ACTIVE")
    private Boolean isUserActive;

    @Column(name = "MEMBER_ACCEPT_ORDER")
    private Boolean memberAcceptOrder;

    @JoinColumn(name = "MERCHANT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Merchant merchant;

    @Column(name = "CONTACT_NUMBER")
	private String contactNumber;

	@JoinColumn(name = "SUPER_USER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private UserProfile superUserId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy ="userId", cascade = CascadeType.ALL)
	private List<UPIDetail> UPIDetails;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getSinglePayoutMinAmount() {
        if (singlePayoutMinAmount == null) {
            return new BigDecimal(0);
        }
        return singlePayoutMinAmount;
    }

    public void setSinglePayoutMinAmount(BigDecimal singlePayoutMinAmount) {
        this.singlePayoutMinAmount = singlePayoutMinAmount;
    }

    public BigDecimal getSinglePayoutMaxAmount() {
        if (singlePayoutMaxAmount == null) {
            return new BigDecimal(0);
        }
        return singlePayoutMaxAmount;
    }

    public void setSinglePayoutMaxAmount(BigDecimal singlePayoutMaxAmount) {
        this.singlePayoutMaxAmount = singlePayoutMaxAmount;
    }

    public BigDecimal getDailyPayoutLimitAmount() {
        if (dailyPayoutLimitAmount == null) {
            return new BigDecimal(0);
        }
        return dailyPayoutLimitAmount;
    }

    public void setDailyPayoutLimitAmount(BigDecimal dailyPayoutLimitAmount) {
        this.dailyPayoutLimitAmount = dailyPayoutLimitAmount;
    }

    public List<UPIDetail> getUPIDetails() {
		return UPIDetails;
	}

	public void setUPIDetails(List<UPIDetail> uPIDetails) {
		UPIDetails = uPIDetails;
	}

	public Long getUserId() {
        return userId;
    }

    public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
 
	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	@Override
    public String getPassword() {
        return password;
    }

	public String getSettlementPassword() {
		return settlementPassword;
	}

	public void setSettlementPassword(String settlementPassword) {
		this.settlementPassword = settlementPassword;
	}

	@Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private Collection<? extends GrantedAuthority> translate(Roles role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String name = role.getName().toUpperCase();
        if (!name.startsWith("ROLE_")) {
            name = "ROLE_" + name;
        }
        authorities.add(new SimpleGrantedAuthority(name));
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return translate(this.role);
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsUserActive() {
        return isUserActive;
    }

    public void setIsUserActive(Boolean isUserActive) {
        this.isUserActive = isUserActive;
    }

	public UserProfile getSuperUserId() {
		return superUserId;
	}

	public void setSuperUserId(UserProfile superUserId) {
		this.superUserId = superUserId;
	}

    public Boolean getMemberAcceptOrder() {
        if (memberAcceptOrder == null) {
            return false;
        }
        return memberAcceptOrder;
    }

    public void setMemberAcceptOrder(Boolean memberAcceptOrder) {
        this.memberAcceptOrder = memberAcceptOrder;
    }
}
