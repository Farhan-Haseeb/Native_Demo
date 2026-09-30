// src/main/ets/utils/FilePicker.ets

import { picker } from '@kit.CoreFileKit';
import { BusinessError } from '@kit.BasicServicesKit';

/**
 * Configuration options of the file picker
 */
export interface SelectOptions {
  /**
   * Array of allowed file suffixes, including the dot.
   * Example: ['.jpg', '.png'], ['.pdf'], ['.doc', '.docx']
   */
  fileSuffixes: string[];
}

/**
 * A minimal, general-purpose single-file picker.
 * Uses DocumentViewPicker directly and filters by file suffix.
 */
export class FilePicker {
  /**
   * Launch the system UI to select a single file.
   *
   * @param options {SelectOptions} Configuration object containing the file suffixes.
   * @returns {Promise<string>} Returns the URI of the selected file on success.
   * @throws {Error} Throws if the user cancels or an error occurs.
   */
  public static async select(options: SelectOptions): Promise<string> {
    const documentPicker = new picker.DocumentViewPicker();

    // Build the picker parameters, filtering directly by suffix
    const pickerOptions: picker.DocumentSelectOptions = {
      maxSelectNumber: 1,
      fileSuffixFilters: options.fileSuffixes,
    };

    try {
      const uris = await documentPicker.select(pickerOptions);

      // Validate the returned result
      if (uris && uris.length > 0) {
        return uris[0]; // Return the single file URI
      } else {
        // The user closed the picker without selecting a file
        throw new Error('No file was selected.');
      }
    } catch (err) {
      const error = err as BusinessError;
      // Error code 100001 is the officially defined “user cancelled the operation”
      if (error.code === 100001) {
        console.info('[FilePicker] User cancelled the selection.');
        throw new Error('User cancelled selection.');
      } else {
        console.error(`[FilePicker] Selection failed, code: ${error.code}, message: ${error.message}`);
        throw new Error('File selection failed.');
      }
    }
  }
}