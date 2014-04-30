/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.java.nio.fs.jgit;

import static org.fest.assertions.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.junit.Test;
import org.uberfire.java.nio.file.DirectoryNotEmptyException;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.java.nio.file.NoSuchFileException;
import org.uberfire.java.nio.file.Path;

public class JGitFileSystemProviderCpMvTest extends AbstractTestInfra {

    @Test
    public void testCopyBranches() throws IOException {
        final URI newRepo = URI.create( "git://copybranch-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        {
            final Path path = provider.getPath( URI.create( "git://master@copybranch-test-repo/myfile1.txt" ) );

            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }

        {
            final Path path2 = provider.getPath( URI.create( "git://user_branch@copybranch-test-repo/other/path/myfile2.txt" ) );

            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        {
            final Path path3 = provider.getPath( URI.create( "git://user_branch@copybranch-test-repo/myfile3.txt" ) );

            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        final Path source = provider.getPath( URI.create( "git://user_branch@copybranch-test-repo" ) );
        final Path target = provider.getPath( URI.create( "git://other_branch@copybranch-test-repo" ) );

        provider.copy( source, target );

        final DirectoryStream<Path> stream = provider.newDirectoryStream( provider.getPath( URI.create( "git://other_branch@copybranch-test-repo/" ) ), null );

        assertThat( stream ).isNotNull().hasSize( 2 );

        try {
            provider.copy( source, target );
            failBecauseExceptionWasNotThrown( FileAlreadyExistsException.class );
        } catch ( FileAlreadyExistsException e ) {
        }

        final Path notExists = provider.getPath( URI.create( "git://xxx_user_branch@copybranch-test-repo" ) );
        final Path notExists2 = provider.getPath( URI.create( "git://xxx_other_branch@copybranch-test-repo" ) );

        try {
            provider.copy( notExists, notExists2 );
            failBecauseExceptionWasNotThrown( NoSuchFileException.class );
        } catch ( NoSuchFileException e ) {
        }
    }

    @Test
    public void testCopyFiles() throws IOException {
        final URI newRepo = URI.create( "git://copyasset-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        final Path path = provider.getPath( URI.create( "git://master@copyasset-test-repo/myfile1.txt" ) );
        {
            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }
        final Path path2 = provider.getPath( URI.create( "git://user_branch@copyasset-test-repo/other/path/myfile2.txt" ) );
        {
            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        final Path path3 = provider.getPath( URI.create( "git://user_branch@copyasset-test-repo/myfile3.txt" ) );
        {
            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        final Path target = provider.getPath( URI.create( "git://user_branch@copyasset-test-repo/myfile1.txt" ) );

        provider.copy( path, target );

        final DirectoryStream<Path> stream = provider.newDirectoryStream( provider.getPath( URI.create( "git://user_branch@copyasset-test-repo/" ) ), null );

        for ( Path path1 : stream ) {
            System.out.println("content: " + path1.toUri());
        }

        assertThat( stream ).isNotNull().hasSize( 3 );
    }

    @Test
    public void testCopyDir() throws IOException {
        final URI newRepo = URI.create( "git://copydir-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        final Path path = provider.getPath( URI.create( "git://master@copydir-test-repo/myfile1.txt" ) );
        {
            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }
        final Path path2 = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/path/myfile2.txt" ) );
        {
            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        final Path path3 = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/path/myfile3.txt" ) );
        {
            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/path" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/" ) );

            provider.copy( source, target );

            final DirectoryStream<Path> stream = provider.newDirectoryStream( target, null );

            assertThat( stream ).isNotNull().hasSize( 3 );
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/path" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/some/place/here/" ) );

            provider.copy( source, target );

            final DirectoryStream<Path> stream = provider.newDirectoryStream( target, null );

            assertThat( stream ).isNotNull().hasSize( 2 );
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/path" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/soXme/place/here" ) );

            provider.copy( source, target );

            final DirectoryStream<Path> stream = provider.newDirectoryStream( target, null );

            assertThat( stream ).isNotNull().hasSize( 2 );
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/other_here/" ) );

            provider.copy( source, target );

            final DirectoryStream<Path> stream = provider.newDirectoryStream( target, null );

            assertThat( stream ).isNotNull().hasSize( 1 );
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/not_exists" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/xxxxxxxxother_here/" ) );

            try {
                provider.copy( source, target );
                failBecauseExceptionWasNotThrown( NoSuchFileException.class );
            } catch ( NoSuchFileException e ) {
            }
        }
        {
            final Path source = provider.getPath( URI.create( "git://user_branch@copydir-test-repo/" ) );
            final Path target = provider.getPath( URI.create( "git://master@copydir-test-repo/other_here/" ) );

            try {
                provider.copy( source, target );
                failBecauseExceptionWasNotThrown( FileAlreadyExistsException.class );
            } catch ( FileAlreadyExistsException e ) {
            }
        }
    }

    @Test
    public void testMoveBranches() throws IOException {
        final URI newRepo = URI.create( "git://movebranch-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        {
            final Path path = provider.getPath( URI.create( "git://master@movebranch-test-repo/myfile1.txt" ) );

            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }

        {
            final Path path2 = provider.getPath( URI.create( "git://user_branch@movebranch-test-repo/other/path/myfile2.txt" ) );

            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        {
            final Path path3 = provider.getPath( URI.create( "git://user_branch@movebranch-test-repo/myfile3.txt" ) );

            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        final Path source = provider.getPath( URI.create( "git://user_branch@movebranch-test-repo/" ) );
        final Path target = provider.getPath( URI.create( "git://master@movebranch-test-repo/" ) );

        try {
            provider.move( source, target );
            failBecauseExceptionWasNotThrown( FileAlreadyExistsException.class );
        } catch ( org.uberfire.java.nio.IOException e ) {
        }

        final Path source2 = provider.getPath( URI.create( "git://user_branch@movebranch-test-repo/" ) );
        final Path target2 = provider.getPath( URI.create( "git://xxxxddddkh@movebranch-test-repo/" ) );

        try {
            provider.move( source2, target2 );
        } catch ( final Exception e ) {
            fail( "should not throw" );
        }
    }

    @Test
    public void testMoveFiles() throws IOException {
        final URI newRepo = URI.create( "git://moveasset-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        final Path path = provider.getPath( URI.create( "git://master@moveasset-test-repo/myfile1.txt" ) );
        {
            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }
        final Path path2 = provider.getPath( URI.create( "git://user_branch@moveasset-test-repo/other/path/myfile2.txt" ) );
        {
            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        final Path path3 = provider.getPath( URI.create( "git://user_branch@moveasset-test-repo/myfile3.txt" ) );
        {
            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        final Path target = provider.getPath( URI.create( "git://user_branch@moveasset-test-repo/myfile1.txt" ) );

        try {
            provider.move( path, target );
        } catch ( final Exception e ) {
            fail( "should move file", e );
        }
    }

    @Test
    public void testMoveDir() throws IOException {
        final URI newRepo = URI.create( "git://movedir-test-repo" );
        provider.newFileSystem( newRepo, EMPTY_ENV );

        final Path path = provider.getPath( URI.create( "git://master@movedir-test-repo/myfile1.txt" ) );
        {
            final OutputStream outStream = provider.newOutputStream( path );
            outStream.write( "my cool content".getBytes() );
            outStream.close();
        }
        final Path path2 = provider.getPath( URI.create( "git://user_branch@movedir-test-repo/path/myfile2.txt" ) );
        {
            final OutputStream outStream2 = provider.newOutputStream( path2 );
            outStream2.write( "my cool content".getBytes() );
            outStream2.close();
        }
        final Path path3 = provider.getPath( URI.create( "git://user_branch@movedir-test-repo/path/myfile3.txt" ) );
        {
            final OutputStream outStream3 = provider.newOutputStream( path3 );
            outStream3.write( "my cool content".getBytes() );
            outStream3.close();
        }

        {
            final Path source = provider.getPath( URI.create( "git://user_branch@movedir-test-repo/path" ) );
            final Path target = provider.getPath( URI.create( "git://master@movedir-test-repo/" ) );

            try {
                provider.move( source, target );
            } catch ( org.uberfire.java.nio.IOException e ) {
                assertThat( e ).isInstanceOf( DirectoryNotEmptyException.class );
            }
        }
    }
}
