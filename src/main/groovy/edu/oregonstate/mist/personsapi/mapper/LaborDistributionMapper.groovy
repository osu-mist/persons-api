package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.LaborDistribution
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class LaborDistributionMapper implements ResultSetMapper<LaborDistribution> {
    public LaborDistribution map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new LaborDistribution (
                effectiveDate: rs.getDate('EFFECTIVE_DATE'),
                accountIndexCode: rs.getString('ACCOUNT_INDEX_CODE'),
                fundCode: rs.getString('FUND_CODE'),
                organizationCode: rs.getString('ORGANIZATION_CODE'),
                accountCode: rs.getString('ACCOUNT_CODE'),
                programCode: rs.getString('PROGRAM_CODE'),
                activityCode: rs.getString('ACTIVITY_CODE'),
                distributionPercent: rs.getBigDecimal('DISTRIBUTION_PERCENT')
        )
    }
}
