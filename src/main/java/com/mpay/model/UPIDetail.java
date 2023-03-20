package com.mpay.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.mpay.enums.PaymentMethod;
import org.hibernate.annotations.SQLDelete;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

@Entity
@Setter
@Getter
@Table(name = "UPI_DETAIL")
@EqualsAndHashCode(callSuper = false)
@SQLDelete(sql = "UPDATE UPI_DETAIL SET is_deleted = true, date_modified = now() WHERE UPI_DETAIL_ID = ?") // Soft
@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
public class UPIDetail extends TrackChangeEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "UPI_DETAIL_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long upiDetailsId;

    @Column(name = "PAYMENT_TYPE")
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod paymentType;

	@Column(name = "IS_DEFAULT", columnDefinition = "boolean default false")
	private Boolean isDefault;

    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProfile userId;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    // UPI fields
    @Column(name = "UPI")
    private String upi;

    @Lob
    @Column(name = "UPI_SCANNER")
    private byte[] upiScanner;

    @Column(name = "UPI_TYPE")
    private String upiType;

    // Ewallet fields
    @Column(name = "EWALLET")
    private String ewallet;

    // Netbanking fields
    @Column(name = "BANK_NAME")
    private String bankName;

    @Column(name = "ACCOUNT_NAME")
    private String accountName;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountNumber;

    @Column(name = "IFSC_CODE")
    private String ifscCode;

    public boolean getIsDefault() {
        if (isDefault == null) {
            return false;
        }
        return isDefault;
    }
}
