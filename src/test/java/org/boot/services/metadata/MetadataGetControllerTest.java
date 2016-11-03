package org.boot.services.metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@AutoConfigureMockMvc
public class MetadataGetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    MetadataRepository repository;

    @Before
    public void setup(){
        repository.deleteAll();
    }

    private RestDocumentationResultHandler restDoc(String name) {
        ContentModifyingOperationPreprocessor jsonSource = new ContentModifyingOperationPreprocessor(new JsonSyntaxHighlighter());
        return document(name,
                preprocessRequest(removeHeaders("Host"),prettyPrint()),
                preprocessResponse(removeHeaders("X-Application-Context","Content-Length"),prettyPrint(),jsonSource));
    }

    @Test
    public void shouldReturnRequestedConfigForAGroup() throws Exception {
        Metadata metadata = new MetadataBuilder().group("mygroup").name("myconfig").value("key1", "value1").value("key2", Arrays.asList("One","Two","Three")).build();
        metadata = repository.save(metadata);
        repository.save(new MetadataBuilder().group("mygroup").name("myconfig2").value("key3", "value3").build());

        ResultActions result = mvc.perform(get("/metadata/mygroup/myconfig" ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(header().string("Cache-Control","max-age=3600, public"))
                .andExpect(jsonPath("$.id",equalTo(metadata.getId().toString())))
                .andExpect(jsonPath("$.group",equalTo("mygroup")))
                .andExpect(jsonPath("$.name",equalTo("myconfig")))
                .andExpect(jsonPath("$.value.key1",equalTo("value1")))
                .andExpect(jsonPath("$.value.key2",equalTo(Arrays.asList("One","Two","Three"))))
                .andDo(restDoc("getMetadataByGroup"));

    }

    @Test
    public void shouldReturnConfigForId() throws Exception {
        Metadata metadata = new MetadataBuilder().group("mygroup").name("myconfig").value("key1", "value1").value("key2", Arrays.asList("One","Two","Three")).build();
        metadata = repository.save(metadata);
        repository.save(new MetadataBuilder().group("mygroup").name("myconfig2").value("key3", "value3").build());

        ResultActions result = mvc.perform(get("/metadata/" + metadata.getId() ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(header().string("Cache-Control","max-age=3600, public"))
                .andExpect(jsonPath("$.id",equalTo(metadata.getId().toString())))
                .andExpect(jsonPath("$.group",equalTo("mygroup")))
                .andExpect(jsonPath("$.name",equalTo("myconfig")))
                .andExpect(jsonPath("$.value.key1",equalTo("value1")))
                .andExpect(jsonPath("$.value.key2",equalTo(Arrays.asList("One","Two","Three"))))
                .andDo(restDoc("getMetadataById"));;

    }

    @Test
    public void shouldReturn404WhenRequestedConfigForAGroupDoesNotExists() throws Exception {
        ResultActions result = mvc.perform(get("/metadata/mygroup/myconfig" ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Cache-Control"))
                .andExpect(jsonPath("$.message",equalTo("Requested metadata entity having id mygroup:myconfig does not exists in the system.")))
                .andDo(restDoc("getMetadataByGroup404"));;

    }

    @Test
    public void shouldReturn404WhenRequestedConfigForIdDoesNotExists() throws Exception {
        ResultActions result = mvc.perform(get("/metadata/507f1f77bcf86cd799439011" ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Cache-Control"))
                .andExpect(jsonPath("$.message",equalTo("Requested metadata entity having id 507f1f77bcf86cd799439011 does not exists in the system.")))
                .andDo(restDoc("getMetadataById404"));;

    }

    @Test
    public void shouldReturnAllConfigsForAGroup() throws Exception {
        repository.save(new MetadataBuilder().group("mygroup").name("myconfig").value("key1", "value1").value("key2", Arrays.asList("One","Two","Three")).build());
        repository.save(new MetadataBuilder().group("mygroup").name("myconfig2").value("key3", "value3").build());

        ResultActions result = mvc.perform(get("/metadata/groups/mygroup" ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(header().string("Cache-Control","max-age=3600, public"))
                .andExpect(jsonPath("$[0].group",equalTo("mygroup")))
                .andExpect(jsonPath("$[0].name",equalTo("myconfig")))
                .andExpect(jsonPath("$[0].value.key1",equalTo("value1")))
                .andExpect(jsonPath("$[0].value.key2",equalTo(Arrays.asList("One","Two","Three"))))
                .andExpect(jsonPath("$[1].group",equalTo("mygroup")))
                .andExpect(jsonPath("$[1].name",equalTo("myconfig2")))
                .andExpect(jsonPath("$[1].value.key3",equalTo("value3")))
                .andDo(restDoc("getAllMetadataForGroup"));;

    }

    @Test
    public void shouldReturnEmptyListForAGroupThatDoesNotExists() throws Exception {

        ResultActions result = mvc.perform(get("/metadata/groups/mygroup" ).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(header().string("Cache-Control","max-age=3600, public"))
                .andExpect(jsonPath("$",hasSize(0)))
                .andDo(restDoc("getAllMetadataForGroupEmpty"));;

    }

}