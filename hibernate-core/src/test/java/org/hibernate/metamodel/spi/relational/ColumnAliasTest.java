/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.spi.relational;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.testing.FailureExpectedWithNewMetamodel;
import org.hibernate.testing.junit4.BaseUnitTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * NOTE: the table number is automatically statically incremented every time
 * a table is created. Because of this, it is impossible to predict how
 * large the table number can get when running the suite of unit tests.
 * Since we don't know how large the table number is, we don't know how
 * many characters to allot in the resulting alias for the table number.
 *
 * To workaround this unknown, Dialect instances are created
 * to control whether the test will need to truncate the column
 * name when creating the alias.
 *
 * @author Gail Badner
 */
public class ColumnAliasTest extends BaseUnitTestCase {

	private Schema schema;
	private Table table0;
	private Table table1;

	@Before
	public void setUp() {
		schema = new Schema( null, null );
		table0 = new Table(
				schema,
				Identifier.toIdentifier( "table0" ),
				Identifier.toIdentifier( "table0" )
		);
		table1 = new Table(
				schema,
				Identifier.toIdentifier( "table1" ),
				Identifier.toIdentifier( "table1" )
		);
	}

	@After
	public void tearDown() {
		schema = null;
		table0 = null;
		table1 = null;
	}

	@Test
	public void testNoCharactersInNameNoTruncation() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 25 );

		Column column = table0.createColumn( "1" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`1`" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );
	}

	public void testNameStartsWithNonCharacterNoTruncation() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 25 );

		Column column = table0.createColumn( "1abc" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "1abc`" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "_abc" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`_abc`" );
		assertEquals( "column" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "column" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );
	}

	@Test
	@FailureExpectedWithNewMetamodel
	public void testNameStartsWithNonCharacterTruncation() {
		Column column = table0.createColumn( "1" );
		String expectedSuffix = getExpectedSuffix( column, null );
		// create dialect with maximum alias length that will force truncation.
		Dialect dialect = createDialect( expectedSuffix.length() + "column".length() - 1 );
		String nameTruncated = "column".substring( 0, dialect.getMaxAliasLength() - expectedSuffix.length() );
		assertTrue( nameTruncated.length() < "column".length() );
		String alias = column.getAlias( dialect, null );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix , alias );

		column = table0.createColumn( "_abc" );
		// create dialect with maximum alias length that will force truncation.
		expectedSuffix = getExpectedSuffix( column, table1 );
		dialect = createDialect( expectedSuffix.length() + "column".length() - 1 );
		nameTruncated = "column".substring( 0, dialect.getMaxAliasLength() - expectedSuffix.length() );
		assertTrue( nameTruncated.length() < "column".length() );
		alias = column.getAlias( dialect, table1 );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix , alias );
	}

	@Test
	@FailureExpectedWithNewMetamodel
	public void testNameIncludingNonCharacter() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 10 );

		Column column = table0.createColumn( "a1" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );
		column = table0.createColumn( "`a1`" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "a1b" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`a1b`" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "a_b" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`a_b`" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "ab1" );
		assertEquals( "ab" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "ab" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`ab1`" );
		assertEquals( "ab" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "ab" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "a1b2" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`a1b2`" );
		assertEquals( "a" + getExpectedSuffix( column, null ) , column.getAlias( dialect, null ) );
		assertEquals( "a" + getExpectedSuffix( column, table1 ) , column.getAlias( dialect, table1 ) );
	}

	@Test
	public void testUseNameAsIs() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 25 );

		Column column = table0.createColumn( "abc" );
		assertEquals( "abc" + getExpectedSuffix( null, null ) , column.getAlias( dialect, null ) );

		column = table0.createColumn( "abc" );
		assertEquals( "abc" + getExpectedSuffix( null, table1 ) , column.getAlias( dialect, table1 ) );
	}

	@Test
	public void testUseNameAsIsWithMaxLengthNoTableSuffix() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 10 );
		String name = "abcdefghij";
		Column column = table0.createColumn( name );
		assertEquals( dialect.getMaxAliasLength(), column.getColumnName().getText().length() );
		assertEquals( name + getExpectedSuffix( null, null ) , column.getAlias( dialect, null ) );
	}

	@Test
	public void testUseNameAsIsWithMaxLengthWithTableSuffix() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 10 );
		String name = "abcdefghij";
		assertEquals( dialect.getMaxAliasLength(), name.length() );
		name = name.substring( 0, name.length() - getExpectedTableSuffix( table1 ).length() );
		Column column = table0.createColumn( name );
		String expectedAlias = name + getExpectedTableSuffix( table1 );
		assertEquals( dialect.getMaxAliasLength(), expectedAlias.length() );
		assertEquals( expectedAlias , column.getAlias( dialect, table1 ) );
	}

	@Test
	public void testQuotedNameAllCharactersNoTrucation() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 10 );

		String name = "`abc`";
		Column column = table0.createColumn( name );
		assertEquals( column.getColumnName().getText() + getExpectedSuffix( column, null ), column.getAlias(
				dialect,
				null
		) );
		assertEquals( column.getColumnName().getText() + getExpectedSuffix( column, table1 ), column.getAlias( dialect, table1 ) );
	}

	@Test
	public void testRowIdNameNoTruncation() {
		// create dialect with a large enough max alias length so there is no trucation.
		final Dialect dialect = createDialect( 25 );

		Column column = table0.createColumn( "RowId" );
		assertEquals( "RowId" + getExpectedSuffix( column, null ), column.getAlias( dialect, null ) );
		assertEquals( "RowId" + getExpectedSuffix( column, table1 ), column.getAlias( dialect, table1 ) );

		column = table0.createColumn( "`rowid`" );
		assertEquals( "rowid" + getExpectedSuffix( column, null ), column.getAlias( dialect, null ) );
		assertEquals( "rowid" + getExpectedSuffix( column, table1 ), column.getAlias( dialect, table1 ) );
	}

	@Test
	public void testRowIdNameTruncation() {
		Column column = table0.createColumn( "RowId" );
		String expectedSuffix = getExpectedSuffix( column, null );
		Dialect dialect = createDialect( column.getColumnName().getText().length() + expectedSuffix.length() - 1 );
		String nameTruncated = "RowId".substring( 0, dialect.getMaxAliasLength() - expectedSuffix.length() );
		assertTrue( nameTruncated.length() < "RowId".length() );
		String alias = column.getAlias( dialect, null );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix, alias );

		expectedSuffix = getExpectedSuffix( column, table1 );
		dialect = createDialect( column.getColumnName().getText().length() + expectedSuffix.length() - 1 );
		nameTruncated = "RowId".substring( 0, dialect.getMaxAliasLength() - expectedSuffix.length() );
		assertTrue( nameTruncated.length() < "column".length() );
		alias = column.getAlias( dialect, table1 );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix , alias );
	}

	@Test
	public void testTruncatedName() {
		Column column = table0.createColumn( "abcdefghijk" );
		String expectedSuffix = getExpectedSuffix( column, null );
		// Force max alias length to be less than the column name to that
		// the name is not used as is (and the expected suffix will be used).
		Dialect dialect = createDialect( column.getColumnName().getText().length() - 1 );

		String nameTruncated =
				column.getColumnName().getText().substring(
						0,
						dialect.getMaxAliasLength() - expectedSuffix.length()
				);
		String alias = column.getAlias( dialect, null );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix, alias );

		expectedSuffix = getExpectedSuffix( column, table1 );
		dialect = createDialect( column.getColumnName().getText().length() - 1 );
		nameTruncated =
				column.getColumnName().getText().substring(
						0,
						dialect.getMaxAliasLength() - expectedSuffix.length()
				);
		alias = column.getAlias( dialect, table1 );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix, alias );
	}

	@Test
	public void testTruncatedQuotedName() {
		Column column = table0.createColumn( "`abcdefghijk`" );
		String expectedSuffix = getExpectedSuffix( column, null );
		Dialect dialect = createDialect( column.getColumnName().getText().length() + expectedSuffix.length() - 1 );
		String nameTruncated =
				column.getColumnName().getText().substring(
						0,
						dialect.getMaxAliasLength() - expectedSuffix.length()
				);
		String alias = column.getAlias( dialect, null );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix, alias );

		expectedSuffix = getExpectedSuffix( column, table1 );
		dialect = createDialect( column.getColumnName().getText().length() + expectedSuffix.length() - 1 );
		nameTruncated =
				column.getColumnName().getText().substring(
						0,
						dialect.getMaxAliasLength() - expectedSuffix.length()
				);
		alias = column.getAlias( dialect, table1 );
		assertEquals( dialect.getMaxAliasLength(), alias.length() );
		assertEquals( nameTruncated + expectedSuffix, alias );
	}

	@Test
	public void testMaxAliasLengthTooSmall() {
		Column column = table0.createColumn( "a" );
		try {
			column.getAlias(
					new Dialect() {
						public int getMaxAliasLength() {
							return 2;
						}
					},
					table1
			);
			fail( "should have failed because max alias length cannot accommodate more than just the unique suffix");
		}
		catch (MappingException exception) {
			// expected
		}
	}

	private Dialect createDialect(final int maxAliasLength) {
		return new Dialect() {
			public int getMaxAliasLength() {
				return maxAliasLength;
			}
		};
	}

	private String getExpectedSuffix(Column column, TableSpecification table) {
		return getExpectedColumnSuffix( column ) + getExpectedTableSuffix( table );
	}

	private String getExpectedColumnSuffix(Column column) {
		return column == null ? "" : String.valueOf( column.getPosition() ) + "_";
	}

	private String getExpectedTableSuffix(TableSpecification table) {
		return table == null ? "" : String.valueOf( table.getTableNumber() ) + "_";
	}
}
