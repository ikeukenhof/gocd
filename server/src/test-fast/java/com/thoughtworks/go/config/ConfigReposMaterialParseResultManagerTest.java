/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.config;

import com.thoughtworks.go.config.remote.PartialConfig;
import com.thoughtworks.go.domain.materials.Modification;
import com.thoughtworks.go.helper.ModificationsMother;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

class ConfigReposMaterialParseResultManagerTest {
    @Test
    void shouldAddResultForAConfigRepoMaterialUponSuccessfulParse() {
        String fingerprint = "repo1";

        ConfigReposMaterialParseResultManager manager = new ConfigReposMaterialParseResultManager();
        Modification modification = modificationFor("rev1");
        PartialConfig partialConfig = new PartialConfig();
        manager.parseSuccess(fingerprint, modification, partialConfig);

        PartialConfigParseResult expectedParseResult = PartialConfigParseResult.parseSuccess(modification, partialConfig);

        assertThat(manager.get(fingerprint), is(expectedParseResult));
        assertThat(manager.get(fingerprint).isSuccessful(), is(true));
    }

    @Test
    void shouldAddResultForAConfigRepoMaterialUponUnsuccessfulParse() {
        String fingerprint = "repo1";

        ConfigReposMaterialParseResultManager manager = new ConfigReposMaterialParseResultManager();
        Modification modification = modificationFor("rev1");
        Exception exception = new Exception("Boom!");
        manager.parseFailed(fingerprint, modification, exception);

        PartialConfigParseResult expectedParseResult = PartialConfigParseResult.parseFailed(modification, exception);

        assertThat(manager.get(fingerprint), is(expectedParseResult));
        assertThat(manager.get(fingerprint).isSuccessful(), is(false));
    }

    @Test
    void shouldReturnNullIfManagerDoesNotContainResultForProvidedConfigRepoMaterialFingerprint() {
        String fingerprint = "repo1";

        ConfigReposMaterialParseResultManager manager = new ConfigReposMaterialParseResultManager();
        assertThat(manager.get(fingerprint), is(nullValue()));
    }

    @Test
    void shouldUpdateTheResultForAConfigRepoMaterialIfResultAlreadyExists_WhenMaterialParseFails() {
        String fingerprint = "repo1";

        ConfigReposMaterialParseResultManager manager = new ConfigReposMaterialParseResultManager();
        Modification modification = modificationFor("rev1");
        PartialConfig partialConfig = new PartialConfig();
        manager.parseSuccess(fingerprint, modification, partialConfig);

        PartialConfigParseResult expectedParseResult = PartialConfigParseResult.parseSuccess(modification, partialConfig);

        assertThat(manager.get(fingerprint), is(expectedParseResult));
        assertThat(manager.get(fingerprint).isSuccessful(), is(true));

        Modification modification2 = modificationFor("rev2");
        Exception exception = new Exception("Boom!");
        manager.parseFailed(fingerprint, modification2, exception);


        PartialConfigParseResult parseResult = manager.get(fingerprint);

        assertThat(parseResult.isSuccessful(), is(false));
        assertThat(parseResult.getLatestParsedModification(), is(modification2));
        assertThat(parseResult.getGoodModification(), is(modification));
        assertThat(parseResult.lastGoodPartialConfig(), is(partialConfig));
        assertThat(parseResult.getLastFailure(), is(exception));
    }

    @Test
    void shouldUpdateTheResultForAConfigRepoMaterialIfResultAlreadyExists_WhenMaterialParseIsFixed() {
        String fingerprint = "repo1";

        ConfigReposMaterialParseResultManager manager = new ConfigReposMaterialParseResultManager();
        Modification modification = modificationFor("rev1");
        Exception exception = new Exception("Boom!");
        manager.parseFailed(fingerprint, modification, exception);

        PartialConfigParseResult expectedParseResult = PartialConfigParseResult.parseFailed(modification, exception);

        assertThat(manager.get(fingerprint), is(expectedParseResult));
        assertThat(manager.get(fingerprint).isSuccessful(), is(false));

        Modification modification2 = modificationFor("rev2");
        PartialConfig partialConfig = new PartialConfig();
        manager.parseSuccess(fingerprint, modification2, partialConfig);

        PartialConfigParseResult parseResult = manager.get(fingerprint);

        assertThat(parseResult.isSuccessful(), is(true));
        assertThat(parseResult.getLatestParsedModification(), is(modification2));
        assertThat(parseResult.getGoodModification(), is(modification2));
        assertThat(parseResult.lastGoodPartialConfig(), is(partialConfig));
        assertThat(parseResult.getLastFailure(), is(nullValue()));
    }

    private Modification modificationFor(String revision) {
        return ModificationsMother.oneModifiedFile(revision);
    }
}