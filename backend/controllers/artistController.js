const Artist = require("../models/Artist");
const { formatArtist } = require("../config/formatters");

exports.getArtists = async (req, res, next) => {
  try {
    const artists = await Artist.find().sort({ name: 1 });
    res.json({ artists: artists.map(formatArtist) });
  } catch (error) {
    next(error);
  }
};
