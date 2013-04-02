package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ResourceTypePostProcessorTest {
    
    @Test
    public void testMapping() {
	ResourceTypePostProcessor processor = new ResourceTypePostProcessor();
        assertEquals("audio", processor.process("Sound "));
        assertEquals("video", processor.process("Moving Image"));
        assertEquals("video", processor.process("Moving image"));
        assertEquals("image", processor.process("Still Image"));
        assertEquals("Image2", processor.process("Image2"));
        assertEquals(null, processor.process(null));
        assertEquals("", processor.process(""));
    }

}
