require 'spec_helper'

context 'A media-entry resource with get_metadata_and_previews permission' do
  before :each do
    @media_entry = FactoryGirl.create :media_entry,
                                      get_metadata_and_previews: true
  end

  context 'a meta datum of type text' do
    before :each do
      @meta_datum_text = FactoryGirl.create :meta_datum_text,
                                            media_entry: @media_entry
    end

    describe 'preconditions' do
      it 'exists' do
        expect(MetaDatum.find @meta_datum_text.id).to be
      end

      it 'belongs to the media-entry' do
        expect(@media_entry.meta_data).to include @meta_datum_text
      end
    end
  end
end
